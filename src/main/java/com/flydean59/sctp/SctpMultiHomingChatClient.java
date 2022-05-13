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
package com.flydean59.sctp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public final class SctpMultiHomingChatClient {

    public static void main(String[] args) throws Exception {
        String clientPrimaryAddress = "127.0.0.1";
        String clientSecondAddress ="127.0.0.2";
        int clientPort = 8001;
        String serverAddress= "127.0.0.1";
        int serverPort= 8000;
        startClient(clientPrimaryAddress,clientSecondAddress,serverAddress,clientPort,serverPort);
    }

    private static void startClient(String clientPrimaryAddress,
                                    String clientSecondAddress,
                                    String serverAddress,
                                    int clientPort,
                                    int serverPort) throws UnknownHostException, InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSctpChannel.class)
                    .option(SctpChannelOption.SCTP_NODELAY, true)
                    .handler(new ChannelInitializer<SctpChannel>() {
                        @Override
                        public void initChannel(SctpChannel ch) throws Exception {
                            ch.pipeline().addLast(
                             new LoggingHandler(LogLevel.INFO),
                                    new SctpChatClientHandler());
                        }
                    });

            InetSocketAddress localAddress = SocketUtils.socketAddress(clientPrimaryAddress, clientPort);
            // 绑定第一个address
            ChannelFuture bindFuture = b.bind(localAddress).sync();

            SctpChannel channel = (SctpChannel) bindFuture.channel();
            InetAddress localSecondaryAddress = SocketUtils.addressByName(clientSecondAddress);
            // 绑定第二个address
            channel.bindAddress(localSecondaryAddress).sync();

            //连接到服务器
            InetSocketAddress remoteAddress = SocketUtils.socketAddress(serverAddress, serverPort);
            ChannelFuture connectFuture = channel.connect(remoteAddress).sync();

            connectFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
