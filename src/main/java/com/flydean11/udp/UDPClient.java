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
package com.flydean11.udp;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * UDP client
 */
@Slf4j
public final class UDPClient {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioDatagramChannel.class)
             .option(ChannelOption.SO_BROADCAST, true)
             .handler(new UDPClientHandler());

            Channel ch = b.bind(0).sync().channel();

            // 将消息广播给UDP服务器
            ch.writeAndFlush(new DatagramPacket(
                    Unpooled.copiedBuffer("开始广播", CharsetUtil.UTF_8),
                    SocketUtils.socketAddress("255.255.255.255", PORT))).sync();

            // 等待channel关闭，如果Channel没在5秒钟之内关闭，则打印异常
            if (!ch.closeFuture().await(5000)) {
                log.info("channel没在5秒内关闭!");
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}
