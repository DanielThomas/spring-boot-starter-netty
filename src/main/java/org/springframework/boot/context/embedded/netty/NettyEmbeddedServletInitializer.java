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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.EventExecutorGroup;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * {@link ChannelInitializer} for {@link org.springframework.boot.context.embedded.netty.NettyEmbeddedServletContainer}.
 *
 * @author Danny Thomas
 */
class NettyEmbeddedServletInitializer extends ChannelInitializer<SocketChannel> {
    private final EventExecutorGroup servletExecutor;
    private final RequestDispatcherHandler requestDispatcherHandler;
    private final NettyEmbeddedContext servletContext;

    NettyEmbeddedServletInitializer(EventExecutorGroup servletExecutor, NettyEmbeddedContext servletContext) {
        this.servletContext = servletContext;
        this.servletExecutor = checkNotNull(servletExecutor);
        requestDispatcherHandler = new RequestDispatcherHandler(servletContext);
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast("codec", new HttpServerCodec(4096, 8192, 8192, false));
        p.addLast("servletInput", new ServletContentHandler(servletContext));
        p.addLast(servletExecutor, "filterChain", requestDispatcherHandler);
    }
}
