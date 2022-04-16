
package com.flydean52.nativetransport.epoll;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * 聊天室客户端
 */
@Slf4j
public final class NativeChatClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8000"));

    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new EpollEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(EpollSocketChannel.class)
             .handler(new NativeChatClientInitializer());

            // 建立连接
            Channel ch = b.connect(HOST, PORT).sync().channel();
            log.info("client channel: {}", ch);

            // 从命令行输入
            ChannelFuture lastWriteFuture = null;
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            for (;;) {
                String line = in.readLine();
                if (line == null) {
                    break;
                }
                // 将从命令行输入的一行字符写到channel中
                lastWriteFuture = ch.writeAndFlush(line + "\r\n");
                // 如果输入'再见'，则等待server端关闭channel
                if ("再见".equalsIgnoreCase(line)) {
                    ch.closeFuture().sync();
                    break;
                }
            }
            // 等待所有的消息都写入channel中
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }
}