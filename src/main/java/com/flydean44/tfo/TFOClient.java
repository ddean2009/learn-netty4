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
package com.flydean44.tfo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.internal.SocketUtils;

import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;

import static io.netty.buffer.Unpooled.directBuffer;

/**
 * TFO netty的客户端
 */
public final class TFOClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_FASTOPEN_CONNECT, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 protected void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     p.addLast(new TFOClientHandler());
                 }
             });

            Channel channel = b.register().sync().channel();
            ByteBuf fastOpenData = directBuffer();
            fastOpenData.writeBytes("TFO message".getBytes(StandardCharsets.UTF_8));
            channel.write(fastOpenData);
            // 连接服务器
            SocketAddress serverAddress =  SocketUtils.socketAddress("127.0.0.1", 8000);
            ChannelFuture f = channel.connect(serverAddress).sync();

            // 等待关闭
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
