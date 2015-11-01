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

import com.google.common.collect.ImmutableList;
import io.netty.channel.ChannelHandlerContext;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Netty {@link AsyncContext}.
 *
 * @author Danny Thomas
 */
class NettyAsyncContext implements AsyncContext {
    private ServletRequest servletRequest;
    private final ChannelHandlerContext ctx;
    private ServletResponse servletResponse;
    private boolean asyncStarted;
    private List<AsyncListener> listeners;

    NettyAsyncContext(ServletRequest servletRequest, ChannelHandlerContext ctx) {
        this.servletRequest = servletRequest;
        this.ctx = ctx;
        this.listeners = new ArrayList<>();
    }

    AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        this.servletRequest = servletRequest;
        this.servletResponse = servletResponse;
        asyncStarted = true;
        return this;
    }

    @Override
    public ServletRequest getRequest() {
        return servletRequest;
    }

    @Override
    public ServletResponse getResponse() {
        return servletResponse;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return true;
    }

    @Override
    public void dispatch() {
        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            String path = request.getServletPath();
            String pathInfo = request.getPathInfo();
            if (null != pathInfo) {
                path += pathInfo;
            }
            dispatch(path);
        }
    }

    @Override
    public void dispatch(String path) {
        dispatch(servletRequest.getServletContext(), path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        final HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        httpRequest.setAttribute(ASYNC_CONTEXT_PATH, httpRequest.getContextPath());
        httpRequest.setAttribute(ASYNC_PATH_INFO, httpRequest.getPathInfo());
        httpRequest.setAttribute(ASYNC_QUERY_STRING, httpRequest.getQueryString());
        httpRequest.setAttribute(ASYNC_REQUEST_URI, httpRequest.getRequestURI());
        httpRequest.setAttribute(ASYNC_SERVLET_PATH, httpRequest.getServletPath());
        final NettyRequestDispatcher dispatcher = (NettyRequestDispatcher) context.getRequestDispatcher(path);
        ctx.executor().submit(new Runnable() {
            @Override
            public void run() {
                try {
                    dispatcher.dispatch(httpRequest, servletResponse);
                    // TODO is this right?
                    for (AsyncListener listener : ImmutableList.copyOf(listeners)) {
                        listener.onComplete(new AsyncEvent(NettyAsyncContext.this));
                    }
                } catch (ServletException | IOException e) {
                    // TODO notify listeners
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void complete() {
        try {
            servletResponse.getOutputStream().close();
        } catch (IOException e) {
            // TODO notify listeners
            e.printStackTrace();
        }
    }

    @Override
    public void start(Runnable run) {
        ctx.executor().submit(run, Object.class);
    }

    @Override
    public void addListener(AsyncListener listener) {
        listeners.add(listener);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {

    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        return null;
    }

    @Override
    public void setTimeout(long timeout) {

    }

    @Override
    public long getTimeout() {
        return 0;
    }

    public boolean isAsyncStarted() {
        return asyncStarted;
    }
}
