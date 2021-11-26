
package com.flydean40.udtByte;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.util.concurrent.ThreadFactory;

/**
 * UDT 客户端
 */
public final class UDTByteEchoClient {

    static final String HOST = "127.0.0.1";
    static final int PORT = 8000;
    static final int SIZE = 256;

    public static void main(String[] args) throws Exception {
        // 客户端连接
        final ThreadFactory connectFactory = new DefaultThreadFactory("connect");
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                connectFactory, NioUdtProvider.BYTE_PROVIDER);
        try {
            final Bootstrap boot = new Bootstrap();
            boot.group(connectGroup)
                    .channelFactory(NioUdtProvider.BYTE_CONNECTOR)
                    .handler(new ChannelInitializer<UdtChannel>() {
                        @Override
                        public void initChannel(final UdtChannel ch) {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new UDTByteEchoClientHandler());
                        }
                    });
            // 启动客户端
            final ChannelFuture f = boot.connect(HOST, PORT).sync();
            // 等待连接关闭
            f.channel().closeFuture().sync();
        } finally {
            // 关闭event loop
            connectGroup.shutdownGracefully();
        }
    }
}
