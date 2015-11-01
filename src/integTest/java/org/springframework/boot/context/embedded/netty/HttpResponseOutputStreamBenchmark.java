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

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks for {@link HttpResponseOutputStream}.
 */
@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@BenchmarkMode(Mode.AverageTime)
public class HttpResponseOutputStreamBenchmark {
    private HttpResponseOutputStream stream;
    private byte[] input;

    @Param({"1024", "2048", "4096", "8192", "16384", "32768"})
    public int size;

    @Setup
    public void setup() {
        HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
        ChannelHandlerContext ctx = new StubChannelHandlerContext();
        NettyEmbeddedContext context = new NettyEmbeddedContext("/", Thread.currentThread().getContextClassLoader(), "Server");
        NettyHttpServletResponse servletResponse = new NettyHttpServletResponse(ctx, context, httpResponse);
        stream = new HttpResponseOutputStream(ctx, servletResponse);
        input = new byte[size];
    }

    @Benchmark
    public void writeByte() throws IOException {
        for (int i = 0; i < size; i++) {
            stream.write(0);
        }
    }

    @Benchmark
    public void writeBytes() throws IOException {
        stream.write(input);
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + HttpResponseOutputStreamBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
