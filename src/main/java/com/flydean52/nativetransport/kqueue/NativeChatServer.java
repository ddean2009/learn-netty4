
package com.flydean52.nativetransport.kqueue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * native chat server 服务器
 */
@Slf4j
public final class NativeChatServer {

    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup bossGroup = new KQueueEventLoopGroup(1);
        EventLoopGroup workerGroup = new KQueueEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(KQueueServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new NativeChatServerInitializer());

            Channel channel = b.bind(PORT).sync().channel();
            log.info("server channel:{}", channel);
            channel.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}