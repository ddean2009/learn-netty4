/*
 * Copyright 2022 learn-netty4 Project
 *
 * The learn-netty4 Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.flydean41.udtMessage;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * UDT Message 服务器端
 */
public final class UDTMsgEchoServer {

    static final int PORT =8000;

    public static void main(String[] args) throws Exception {
        final ThreadFactory acceptFactory = new DefaultThreadFactory("accept");
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup acceptGroup =
                new NioEventLoopGroup(1, acceptFactory, NioUdtProvider.MESSAGE_PROVIDER);
        final NioEventLoopGroup connectGroup =
                new NioEventLoopGroup(1, connectFactory, NioUdtProvider.MESSAGE_PROVIDER);

        try {
            final ServerBootstrap boot = new ServerBootstrap();
            boot.group(acceptGroup, connectGroup)
                    .channelFactory(NioUdtProvider.MESSAGE_ACCEPTOR)
                    .option(ChannelOption.SO_BACKLOG, 10)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<UdtChannel>() {
                        @Override
                        public void initChannel(final UdtChannel ch)
                                throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new UDTMsgEchoServerHandler());
                        }
                    });
            // 启动服务器
            final ChannelFuture future = boot.bind(PORT).sync();
            future.channel().closeFuture().sync();
        } finally {
            acceptGroup.shutdownGracefully();
            connectGroup.shutdownGracefully();
        }
    }
}
