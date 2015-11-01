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

import io.netty.handler.codec.http.*;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;

/**
 * Sample benchmark to check that the Gradle configuration is working.
 */
@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
public class NettyHttpServletResponseBenchmark {
    private NettyHttpServletResponse response;

    @Setup
    public void setup() {
        StubChannelHandlerContext cxt = new StubChannelHandlerContext();
        HttpResponse httpResponse = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
        NettyEmbeddedContext context = new NettyEmbeddedContext("/", Thread.currentThread().getContextClassLoader(), "Server");
        response = new NettyHttpServletResponse(cxt, context, httpResponse);
    }

    @Benchmark
    public void setContentType() {
        response.setContentType("text/html");
    }

    @Benchmark
    public void setContentTypeHeader() {
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE, "text/html");
    }

    @Benchmark
    public CharSequence getFormattedDate() {
        return response.getFormattedDate();
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + NettyHttpServletResponseBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(5)
                .measurementIterations(5)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
