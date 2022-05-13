package com.flydean51.affinity;
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
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;
import net.openhft.affinity.AffinityStrategies;
import net.openhft.affinity.AffinityThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * @author wayne
 * @version AffinityServer,  2021/8/1
 */
@AllArgsConstructor
public class AffinityServer {

    final int acceptorThreads = 2;
    final int workerThreads = 5;

    private final int port;

    public void start() throws InterruptedException {

        //建立两个EventloopGroup用来处理连接和消息
        EventLoopGroup acceptorGroup = new NioEventLoopGroup(acceptorThreads);
        //创建AffinityThreadFactory
        ThreadFactory threadFactory = new AffinityThreadFactory("affinityWorker", AffinityStrategies.DIFFERENT_CORE,AffinityStrategies.DIFFERENT_SOCKET,AffinityStrategies.ANY);
        //将AffinityThreadFactory加入workerGroup
        EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreads,threadFactory);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(acceptorGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new AffinityServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并开始接收连接
            ChannelFuture f = b.bind(port).sync();

            // 等待server socket关闭
            f.channel().closeFuture().sync();
        } finally {
            //关闭group
            workerGroup.shutdownGracefully();
            acceptorGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port=8000;
        new AffinityServer(port).start();
    }

}
