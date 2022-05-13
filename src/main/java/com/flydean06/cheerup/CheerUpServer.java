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
package com.flydean06.cheerup;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 收到Client的消息之后会输出"加油"
 */
public final class CheerUpServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "20"));

    public static void main(String[] args) throws Exception {

        // Server配置
        //boss loop
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        //worker loop
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        final CheerUpServerHandler serverHandler = new CheerUpServerHandler();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
              // tcp/ip协议listen函数中的backlog参数,等待连接池的大小
             .option(ChannelOption.SO_BACKLOG, 100)
              //日志处理器
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 //初始化channel，添加handler
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     //日志处理器
                     p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast(serverHandler);
                 }
             });

            // 启动服务器
            ChannelFuture f = b.bind(PORT).sync();

            // 等待channel关闭
            f.channel().closeFuture().sync();
        } finally {
            // 关闭所有的event loop
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
