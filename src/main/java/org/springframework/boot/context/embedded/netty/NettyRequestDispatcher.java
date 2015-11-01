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

import javax.servlet.*;
import java.io.IOException;

/**
 * @author Danny Thomas
 */
class NettyRequestDispatcher implements RequestDispatcher {
    private final ServletContext context;
    private final FilterChain filterChain;

    NettyRequestDispatcher(ServletContext context, FilterChain filterChain) {
        this.context = context;
        this.filterChain = filterChain;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.FORWARD);
        // TODO implement
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.INCLUDE);
        // TODO implement
    }

    void dispatch(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        request.setAttribute(NettyHttpServletRequest.DISPATCHER_TYPE, DispatcherType.ASYNC);
        filterChain.doFilter(request, response);
    }
}
