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

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletException;
import java.util.Collection;
import java.util.EnumSet;

/**
 * A {@link org.springframework.boot.context.embedded.netty.NettyEmbeddedContext} {@link javax.servlet.FilterRegistration}.
 *
 * @author Danny Thomas
 */
class NettyFilterRegistration extends AbstractNettyRegistration implements FilterRegistration.Dynamic {
    private volatile boolean initialised;
    private Filter filter;

    NettyFilterRegistration(NettyEmbeddedContext context, String filterName, String className, Filter filter) {
        super(filterName, className, context);
        this.filter = filter;
    }

    public Filter getFilter() throws ServletException {
        if (!initialised) {
            synchronized (this) {
                if (!initialised) {
                    if (null == filter) {
                        try {
                            filter = (Filter) Class.forName(getClassName()).newInstance();
                        } catch (Exception e) {
                            throw new ServletException(e);
                        }
                    }
                    filter.init(this);
                    initialised = true;
                }
            }
        }
        return filter;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {

    }

    @Override
    public Collection<String> getServletNameMappings() {
        return null;
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        NettyEmbeddedContext context = getNettyContext();
        for (String urlPattern : urlPatterns) {
            context.addFilterMapping(dispatcherTypes, isMatchAfter, urlPattern);
        }
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return null;
    }
}
