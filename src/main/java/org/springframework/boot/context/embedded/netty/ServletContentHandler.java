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
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;

/**
 * {@link io.netty.channel.ChannelInboundHandler} responsible for initial request handling, and getting received
 * {@link HttpContent} messages to the {@link HttpContentInputStream} for the request.
 */
class ServletContentHandler extends ChannelInboundHandlerAdapter {
    private final NettyEmbeddedContext servletContext;
    private HttpContentInputStream inputStream; // FIXME this feels wonky, need a better approach

    ServletContentHandler(NettyEmbeddedContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        inputStream = new HttpContentInputStream(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof HttpRequest) {
            HttpRequest request = (HttpRequest) msg;
            HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, false);
            HttpHeaders.setKeepAlive(response, HttpHeaders.isKeepAlive(request));
            NettyHttpServletResponse servletResponse = new NettyHttpServletResponse(ctx, servletContext, response);
            NettyHttpServletRequest servletRequest = new NettyHttpServletRequest(ctx, servletContext, request, servletResponse, inputStream);
            if (HttpHeaders.is100ContinueExpected(request)) {
                ctx.write(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE), ctx.voidPromise());
            }
            ctx.fireChannelRead(servletRequest);
        }
        if (msg instanceof HttpContent) {
            inputStream.addContent((HttpContent) msg);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        inputStream.close();
    }
}