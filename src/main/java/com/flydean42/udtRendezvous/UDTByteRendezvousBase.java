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
package com.flydean42.udtRendezvous;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public class UDTByteRendezvousBase {

    protected final int messageSize;
    protected final SocketAddress myAddress;
    protected final SocketAddress peerAddress;

    public UDTByteRendezvousBase(int messageSize, SocketAddress myAddress, SocketAddress peerAddress) {
        this.messageSize = messageSize;
        this.myAddress = myAddress;
        this.peerAddress = peerAddress;
    }

    public void run() throws Exception {
        final ThreadFactory connectFactory = new DefaultThreadFactory("rendezvous");
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                connectFactory, NioUdtProvider.BYTE_PROVIDER);
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(connectGroup)
                    .channelFactory(NioUdtProvider.BYTE_RENDEZVOUS)
                    .handler(new ChannelInitializer<UdtChannel>() {
                        @Override
                        protected void initChannel(UdtChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new UDTByteHandler(messageSize));
                        }
                    });
            final ChannelFuture future = bootstrap.connect(peerAddress, myAddress).sync();
            future.channel().closeFuture().sync();
        } finally {
            connectGroup.shutdownGracefully();
        }
    }
}
