
package com.flydean06.cheerup;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 客户端
 */
public final class ChinaClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8007"));
    static final int SIZE = Integer.parseInt(System.getProperty("size", "20"));

    public static void main(String[] args) throws Exception {

        // 客户端的eventLoop
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ChannelPipeline p = ch.pipeline();
                     //添加日志处理器
                     p.addLast(new LoggingHandler(LogLevel.INFO));
                     p.addLast(new ChinaClientHandler());
                 }
             });

            // 启动客户端
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // 等待channel关闭
            f.channel().closeFuture().sync();
        } finally {
            // 关闭所有的event loop
            group.shutdownGracefully();
        }
    }
}
