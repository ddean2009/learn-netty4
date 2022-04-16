
package com.flydean59.sctp;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpServerChannel;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;


public final class SctpMultiHomingChatServer {

    public static void main(String[] args) throws Exception {
        String primaryAddress = "127.0.0.1";
        String secondAddress = "127.0.0.2";
        int port=8000;
        startServer(primaryAddress, secondAddress, port);
    }

    private static void startServer(String primaryAddress, String secondAddress, int port) throws UnknownHostException, InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioSctpServerChannel.class)
             .option(ChannelOption.SO_BACKLOG, 100)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new ChannelInitializer<SctpChannel>() {
                 @Override
                 public void initChannel(SctpChannel ch){
                     ch.pipeline().addLast(
                             new LoggingHandler(LogLevel.INFO),
                             new SctpChatServerHandler());
                 }
             });

            InetSocketAddress localAddress = SocketUtils.socketAddress(primaryAddress, port);
            InetAddress localSecondaryAddress = SocketUtils.addressByName(secondAddress);

            // 绑定第一个地址
            ChannelFuture bindFuture = b.bind(localAddress).sync();
            SctpServerChannel channel = (SctpServerChannel) bindFuture.channel();

            //绑定第二个地址
            ChannelFuture connectFuture = channel.bindAddress(localSecondaryAddress).sync();
            connectFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
