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
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * A {@link org.springframework.boot.context.embedded.netty.NettyEmbeddedContext} {@link javax.servlet.ServletRegistration}.
 *
 * @author Danny Thomas
 */
class NettyServletRegistration extends AbstractNettyRegistration implements ServletRegistration.Dynamic {
    private volatile boolean initialised;
    private Servlet servlet;

    NettyServletRegistration(NettyEmbeddedContext context, String servletName, String className, Servlet servlet) {
        super(servletName, className, context);
        this.servlet = servlet;
    }

    public Servlet getServlet() throws ServletException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {
                    if (null == servlet) {
                        try {
                            servlet = (Servlet) Class.forName(getClassName()).newInstance();
                        } catch (Exception e) {
                            throw new ServletException(e);
                        }
                    }
                    servlet.init(this);
                    initialised = true;
                }
            }
        }
        return servlet;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {

    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {

    }

    @Override
    public void setRunAsRole(String roleName) {

    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        // TODO check for conflicts

        NettyEmbeddedContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addServletMapping(urlPattern, getName());
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getMappings() {
        return null;
    }

    @Override
    public String getRunAsRole() {
        return null;
    }
}
