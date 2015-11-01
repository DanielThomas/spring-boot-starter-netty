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

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for {@link HttpContentInputStream}.
 */
@State(Scope.Group)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class HttpContentInputStreamBenchmark {
    private HttpContentInputStream stream;
    private byte[] b;

    @Param("8192")
    private int size;

    @Setup
    public void setup() {
        stream = new HttpContentInputStream(new EmbeddedChannel());
        b = new byte[size];
    }

    @Benchmark
    @Group("handler")
    public void addContent() {
        stream.addContent(new DefaultHttpContent(Unpooled.buffer(size)));
    }

    @Benchmark
    @Group("handler")
    public int read() throws IOException {
        return stream.read(b);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + HttpContentInputStream.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(0)
                .build();

        new Runner(opt).run();
    }
}
