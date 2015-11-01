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
import io.netty.handler.codec.http.HttpRequest;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

/**
 * {@link HttpServletRequest} wrapper for Netty's {@link HttpRequest}.
 *
 * @author Danny Thomas
 */
class NettyHttpServletRequest implements HttpServletRequest {
    public static final String DISPATCHER_TYPE = NettyRequestDispatcher.class.getName() + ".DISPATCHER_TYPE";

    private final ChannelHandlerContext ctx;
    private final NettyEmbeddedContext servletContext;
    private final HttpRequest request;
    private final ServletInputStream inputStream;
    private final Map<String, Object> attributes;

    private boolean asyncSupported = true;
    private NettyAsyncContext asyncContext;
    private HttpServletResponse servletResponse;

    NettyHttpServletRequest(ChannelHandlerContext ctx, NettyEmbeddedContext servletContext, HttpRequest request, HttpServletResponse servletResponse, ServletInputStream inputStream) {
        this.ctx = ctx;
        this.servletContext = servletContext;
        this.request = request;
        this.servletResponse = servletResponse;
        this.inputStream = inputStream;
        this.attributes = new HashMap<>();
    }

    HttpRequest getNettyRequest() {
        return request;
    }

    @Override
    public String getAuthType() {
        return null;
    }

    @Override
    public Cookie[] getCookies() {
        return new Cookie[0];
    }

    @Override
    public long getDateHeader(String name) {
        return 0;
    }

    @Override
    public String getHeader(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.emptyEnumeration();
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.emptyEnumeration();
    }

    @Override
    public int getIntHeader(String name) {
        return 0;
    }

    @Override
    public String getMethod() {
        return request.method().name();
    }

    @Override
    public String getPathInfo() {
        return null;
    }

    @Override
    public String getPathTranslated() {
        return null;
    }

    @Override
    public String getContextPath() {
        String requestURI = getRequestURI();
        // FIXME implement properly
        return "/".equals(requestURI) ? "" : requestURI;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public String getRemoteUser() {
        return null;
    }

    @Override
    public boolean isUserInRole(String role) {
        return false;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        return null;
    }

    @Override
    public String getRequestURI() {
        return request.uri();
    }

    @Override
    public StringBuffer getRequestURL() {
        return null;
    }

    @Override
    public String getServletPath() {
        return getRequestURI();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public void login(String username, String password) throws ServletException {

    }

    @Override
    public void logout() throws ServletException {

    }

    @Override
    public Collection<Part> getParts() throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public Part getPart(String name) throws IOException, IllegalStateException, ServletException {
        return null;
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        return null;
    }

    @Override
    public Object getAttribute(String name) {
        synchronized (attributes) {
            return attributes.get(name);
        }
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        synchronized (attributes) {
            return Collections.enumeration(attributes.keySet());
        }
    }

    @Override
    public String getCharacterEncoding() {
        return null;
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {

    }

    @Override
    public int getContentLength() {
        return 0;
    }

    @Override
    public long getContentLengthLong() {
        return 0;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return inputStream;
    }

    @Override
    public String getParameter(String name) {
        return null;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return null;
    }

    @Override
    public String[] getParameterValues(String name) {
        return new String[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return null;
    }

    @Override
    public String getProtocol() {
        return request.protocolVersion().protocolName();
    }

    @Override
    public String getScheme() {
        return null;
    }

    @Override
    public String getServerName() {
        return null;
    }

    @Override
    public int getServerPort() {
        return 0;
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return null;
    }

    @Override
    public String getRemoteAddr() {
        return null;
    }

    @Override
    public String getRemoteHost() {
        return null;
    }

    @Override
    public void setAttribute(String name, Object o) {
        synchronized (attributes) {
            attributes.put(name, o);
        }
    }

    @Override
    public void removeAttribute(String name) {
        synchronized (attributes) {
            attributes.remove(name);
        }
    }

    @Override
    public Locale getLocale() {
        return null;
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return 0;
    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public String getLocalAddr() {
        return null;
    }

    @Override
    public int getLocalPort() {
        return 0;
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() {
        return startAsync(this, null);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) {
        return ((NettyAsyncContext) getAsyncContext()).startAsync(servletRequest, servletResponse);
    }

    @Override
    public boolean isAsyncStarted() {
        return null != asyncContext && asyncContext.isAsyncStarted();
    }

    void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    @Override
    public AsyncContext getAsyncContext() {
        if (null == asyncContext) {
            asyncContext = new NettyAsyncContext(this, ctx);
        }
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return attributes.containsKey(DISPATCHER_TYPE) ? (DispatcherType) attributes.get(DISPATCHER_TYPE) : DispatcherType.REQUEST;
    }

    public ServletResponse getServletResponse() {
        return servletResponse;
    }
}
