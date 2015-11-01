/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package org.springframework.boot.context.embedded.netty;

import com.google.common.primitives.Ints;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.LastHttpContent;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link javax.servlet.ServletInputStream} that allows reading from {@link Queue} of {@link HttpContent}, adapting
 * Netty HTTP codec POJOs into a stream readable by servlets.
 *
 * @author Danny Thomas
 */
class HttpContentInputStream extends ServletInputStream {
    private final Channel channel;
    private AtomicBoolean closed;
    private final BlockingQueue<HttpContent> queue;
    private HttpContent current;
    private ReadListener readListener;

    HttpContentInputStream(Channel channel) {
        this.channel = checkNotNull(channel);
        this.closed = new AtomicBoolean();
        queue = new LinkedBlockingQueue<>();
    }

    public void addContent(HttpContent httpContent) {
        checkNotClosed();
        queue.offer(httpContent.retain());
        // TODO limit the number of queued inputs, stop handler from reading from channel
    }

    @Override
    public int readLine(byte[] b, int off, int len) throws IOException {
        checkNotNull(b);
        // TODO use ByteBuf native approach, i.e. bytesBefore, ByteBufProcessor
        return super.readLine(b, off, len);
    }

    @Override
    public boolean isFinished() {
        checkNotClosed();
        return isLastContent() && current.content().readableBytes() == 0;
    }

    private boolean isLastContent() {
        return current instanceof LastHttpContent;
    }

    @Override
    public boolean isReady() {
        checkNotClosed();
        return (current != null && current.content().readableBytes() > 0) || !queue.isEmpty();
    }

    @Override
    public void setReadListener(ReadListener readListener) {
        checkNotClosed();
        checkNotNull(readListener);
        this.readListener = readListener;
    }

    @Override
    public long skip(long n) throws IOException {
        checkNotClosed();
        // TODO implement skip that doesn't read bytes
        return readContent(Ints.checkedCast(n)).readableBytes();
    }

    @Override
    public int available() throws IOException {
        return null == current ? 0 : current.content().readableBytes();
    }

    @Override
    public void close() throws IOException {
        if (closed.compareAndSet(false, true)) {
            // FIXME release the non-written HttpContents?
            queue.clear();
            current = null;
        }
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        checkNotNull(b);
        if (0 == len) {
            return 0;
        }
        poll();
        if (isFinished()) {
            return -1;
        }
        ByteBuf byteBuf = readContent(len);
        int readableBytes = byteBuf.readableBytes();
        byteBuf.readBytes(b, off, readableBytes);
        return readableBytes - byteBuf.readableBytes();
    }

    @Override
    public int read() throws IOException {
        poll();
        if (isFinished()) {
            return -1;
        }
        return readContent(1).getByte(0);
    }

    private ByteBuf readContent(int length) throws IOException {
        ByteBuf content = current.content();
        if (length < content.readableBytes()) {
            return content.readSlice(length);
        } else {
            return content;
        }
    }

    private void poll() throws IOException {
        checkNotClosed();
        if (null == current || current.content().readableBytes() == 0) {
            boolean blocking = null == readListener;
            while (!isLastContent()) {
                try {
                    // FIXME add appropriate timeout value
                    current = queue.poll(0, TimeUnit.NANOSECONDS);
                } catch (InterruptedException ignored) {
                }
                if (current != null || !blocking) {
                    break;
                }
                if (!channel.isActive()) {
                    throw new IOException("Channel is not active");
                }
            }
        }
    }

    private void checkNotClosed() {
        if (closed.get()) {
            throw new IllegalStateException("Stream is closed");
        }
    }
}
