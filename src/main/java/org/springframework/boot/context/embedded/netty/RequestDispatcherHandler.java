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

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A {@link io.netty.channel.ChannelInboundHandler} that bridges to and from {@link HttpServletRequest}s and
 * {@link HttpServletResponse}s from Netty HTTP codec objects.
 *
 * @author Danny Thomas
 */
@ChannelHandler.Sharable
class RequestDispatcherHandler extends SimpleChannelInboundHandler<NettyHttpServletRequest> {
    private final Log logger = LogFactory.getLog(getClass());
    private final NettyEmbeddedContext context;

    RequestDispatcherHandler(NettyEmbeddedContext context) {
        this.context = checkNotNull(context);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyHttpServletRequest request) throws Exception {
        HttpServletResponse servletResponse = (HttpServletResponse) request.getServletResponse();
        try {
            NettyRequestDispatcher dispatcher = (NettyRequestDispatcher) context.getRequestDispatcher(request.getRequestURI());
            if (dispatcher == null) {
                servletResponse.sendError(404);
                return;
            }
            dispatcher.dispatch(request, servletResponse);
        } finally {
            if (!request.isAsyncStarted()) {
                servletResponse.getOutputStream().close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Unexpected exception caught during request", cause);
        ctx.close();
    }
}
