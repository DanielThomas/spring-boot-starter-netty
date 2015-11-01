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

package org.springframework.boot.context.embedded.netty

import com.google.common.base.Charsets
import io.netty.buffer.Unpooled
import io.netty.channel.Channel
import io.netty.handler.codec.http.DefaultHttpContent
import io.netty.handler.codec.http.LastHttpContent
import spock.lang.Specification

/**
 * Tests for {@link HttpContentInputStream}.
 */
class HttpContentInputStreamTest extends Specification {
    def channel
    def stream

    def setup() {
        channel = Mock(Channel)
        stream = new HttpContentInputStream(channel)
    }

    def 'a read with a an empty last content returns -1'() {
        given:
        stream.addContent(LastHttpContent.EMPTY_LAST_CONTENT)

        expect:
        stream.read() == -1
    }

    def 'a read with an empty last content, for a non-active channel, returns -1'() {
        given:
        stream.addContent(LastHttpContent.EMPTY_LAST_CONTENT)

        expect:
        stream.read() == -1
    }

    def 'an attempt to read with a non-active channel, throws IOException'() {
        given:
        channel.isActive() >> false

        when:
        stream.read()

        then:
        thrown(IOException)
    }

    def 'expected number of bytes and content is read for a single content read'() {
        when:
        stream.addContent(new DefaultHttpContent(Unpooled.copiedBuffer("My hovercraft is full of eels.", Charsets.UTF_8)))

        then:
        def b = new byte[30]
        stream.read(b, 0, 30) == 30
        new String(b) == "My hovercraft is full of eels."
    }

    def 'the expected number of bytes and content is read for partial read of two component contents'() {
        when:
        stream.addContent(new DefaultHttpContent(Unpooled.copiedBuffer("My hovercraft is full of eels.", Charsets.UTF_8)))
        stream.addContent(new DefaultHttpContent(Unpooled.copiedBuffer(" I will not buy this record, it is scratched.", Charsets.UTF_8)))

        then:
        def b = new byte[75]
        stream.read(b, 0, 30) == 30
        stream.read(b, 30, 45) == 45
        new String(b) == "My hovercraft is full of eels. I will not buy this record, it is scratched."
    }

    def 'zero length read returns 0'() {
        expect:
        stream.read(new byte[0], 0, 0) == 0
    }

    def 'stream is not finished'() {
        expect:
        !stream.isFinished()
    }

    def 'stream is not ready'() {
        expect:
        !stream.isReady()
    }

    def 'stream has no available bytes'() {
        expect:
        stream.available() == 0
    }
}
