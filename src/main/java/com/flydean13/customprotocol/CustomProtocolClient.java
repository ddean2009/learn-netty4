
package com.flydean13.customprotocol;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * 自定义协议客户端
 */
@Slf4j
public final class CustomProtocolClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));
    static final int COUNT = Integer.parseInt(System.getProperty("count", "100"));

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new CustomProtocolClientInitializer());

            // 建立连接
            ChannelFuture f = b.connect(HOST, PORT).sync();

            // 获取自定义handler
            CustomProtocolClientHandler handler =
                (CustomProtocolClientHandler) f.channel().pipeline().last();

            // 打印结果
            log.info("2的{}次方是:{}",COUNT, handler.getResult());
        } finally {
            group.shutdownGracefully();
        }
    }
}
