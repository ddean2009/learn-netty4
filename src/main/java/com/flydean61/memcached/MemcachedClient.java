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
package com.flydean61.memcached;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class MemcachedClient {

    public static void main(String[] args) throws Exception {
        String host= "127.0.0.1";
        int port = 11211;
        startClient(host, port);
    }

    private static void startClient(String host, int port) throws InterruptedException, IOException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new MemcachedInitializer());
            //启动客户端
            Channel ch = b.connect(host, port).sync().channel();
            // 从命令行读取命令
            System.out.println("输入命令 (退出请输入quit)");
            System.out.println("get <key>");
            System.out.println("set <key> <value>");
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                if ("quit".equalsIgnoreCase(line)) {
                    ch.close().sync();
                    break;
                }
                //发送接受到的命令到服务器端
                lastWriteFuture = ch.writeAndFlush(line);
            }

            // 等等所有消息都同步
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }

}
