
package com.flydean17.protobuf;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 发送一个Student的对象到server端
 */
@Slf4j
public final class StudentClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new StudentClientInitializer());
            // 建立连接
            Channel ch = b.connect(HOST, PORT).sync().channel();
            // 等待关闭
            ch.closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
