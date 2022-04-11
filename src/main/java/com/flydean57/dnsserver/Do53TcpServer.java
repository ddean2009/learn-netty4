
package com.flydean57.dnsserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class Do53TcpServer {

    public static void main(String[] args) throws Exception {
        int dnsServerPort = 53;
        startServer(dnsServerPort);
    }

    private static void startServer(int dnsServerPort) throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        ServerBootstrap bootstrap = new ServerBootstrap().group(bossGroup,
                        workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new Do53ServerChannelInitializer());
        final Channel channel = bootstrap.bind(dnsServerPort).channel();
        channel.closeFuture().sync();
    }

}
