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

import com.google.common.base.StandardSystemProperty;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.context.embedded.EmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerException;

import java.net.InetSocketAddress;

/**
 * An {@link EmbeddedServletContainer} used to control an embedded Netty instance, that bridges to
 * {@link javax.servlet.http.HttpServletRequest} and from {@link javax.servlet.http.HttpServletResponse}
 * to Netty HTTP codec {@link io.netty.handler.codec.http.HttpMessage}s.
 * <p>
 * This is a minimal Servlet 3.1 implementation to provide for the opinionated embedded servlet container model for
 * Spring Boot, supporting a single context, runtime {@link javax.servlet.Registration} only, and no default or JSP
 * servlets.
 * <p>
 * This class should be created using the {@link NettyEmbeddedServletContainerFactory}.
 *
 * @author Danny Thomas
 */
public class NettyEmbeddedServletContainer implements EmbeddedServletContainer {
    private final Log logger = LogFactory.getLog(getClass());
    private final InetSocketAddress address;
    private final NettyEmbeddedContext context;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private DefaultEventExecutorGroup servletExecutor;

    public NettyEmbeddedServletContainer(InetSocketAddress address, NettyEmbeddedContext context) {
        this.address = address;
        this.context = context;
    }

    @Override
    public void start() throws EmbeddedServletContainerException {
        ServerBootstrap b = new ServerBootstrap();
        groups(b);
        servletExecutor = new DefaultEventExecutorGroup(50);
        b.childHandler(new NettyEmbeddedServletInitializer(servletExecutor, context));

        // Don't yet need the complexity of lifecycle state, listeners etc, so tell the context it's initialised here
        context.setInitialised(true);

        ChannelFuture future = b.bind(address).awaitUninterruptibly();
        //noinspection ThrowableResultOfMethodCallIgnored
        Throwable cause = future.cause();
        if (null != cause) {
            throw new EmbeddedServletContainerException("Could not start Netty server", cause);
        }
        logger.info(context.getServerInfo() + " started on port: " + getPort());
    }

    private void groups(ServerBootstrap b) {
        if (StandardSystemProperty.OS_NAME.value().equals("Linux")) {
            bossGroup = new EpollEventLoopGroup(1);
            workerGroup = new EpollEventLoopGroup();
            b.channel(EpollServerSocketChannel.class)
                    .group(bossGroup, workerGroup)
                    .option(EpollChannelOption.TCP_CORK, true);
        } else {
            bossGroup = new NioEventLoopGroup(1);
            workerGroup = new NioEventLoopGroup();
            b.channel(NioServerSocketChannel.class)
                    .group(bossGroup, workerGroup);
        }
        b.option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, 100);
        logger.info("Bootstrap configuration: " + b.toString());
    }

    @Override
    public void stop() throws EmbeddedServletContainerException {
        try {
            if (null != bossGroup) {
                bossGroup.shutdownGracefully().await();
            }
            if (null != workerGroup) {
                workerGroup.shutdownGracefully().await();
            }
            if (null != servletExecutor) {
                servletExecutor.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            throw new EmbeddedServletContainerException("Container stop interrupted", e);
        }
    }

    @Override
    public int getPort() {
        return address.getPort();
    }
}
