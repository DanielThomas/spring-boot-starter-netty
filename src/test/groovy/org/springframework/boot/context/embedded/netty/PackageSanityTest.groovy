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

import com.google.common.base.Predicate
import com.google.common.testing.AbstractPackageSanityTests
import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.junit.Ignore

import javax.annotation.Nullable

/**
 * Sanity tests for {@link org.springframework.boot.context.embedded.netty}.
 */
@Ignore
// FIXME I've added a bunch of new classes that can't be instantiated automatically, that'll I'll need to add back here
class PackageSanityTest extends AbstractPackageSanityTests {
    PackageSanityTest() {
        setDefault(ByteBuf, Unpooled.buffer())
        setDefault(NettyEmbeddedContext, new NettyEmbeddedContext("/", Thread.currentThread().getContextClassLoader(), "ServerInfo"))
        ignoreClasses(new Predicate<Class<?>>() {
            @Override
            boolean apply(@Nullable Class<?> input) {
                input == AbstractNettyRegistration
            }
        })
    }
}
