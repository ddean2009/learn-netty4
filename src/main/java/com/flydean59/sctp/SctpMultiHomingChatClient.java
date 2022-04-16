
package com.flydean59.sctp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.SctpChannelOption;
import io.netty.channel.sctp.nio.NioSctpChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.internal.SocketUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

public final class SctpMultiHomingChatClient {

    public static void main(String[] args) throws Exception {
        String clientPrimaryAddress = "127.0.0.1";
        String clientSecondAddress ="127.0.0.2";
        int clientPort = 8001;
        String serverAddress= "127.0.0.1";
        int serverPort= 8000;
        startClient(clientPrimaryAddress,clientSecondAddress,serverAddress,clientPort,serverPort);
    }

    private static void startClient(String clientPrimaryAddress,
                                    String clientSecondAddress,
                                    String serverAddress,
                                    int clientPort,
                                    int serverPort) throws UnknownHostException, InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSctpChannel.class)
                    .option(SctpChannelOption.SCTP_NODELAY, true)
                    .handler(new ChannelInitializer<SctpChannel>() {
                        @Override
                        public void initChannel(SctpChannel ch) throws Exception {
                            ch.pipeline().addLast(
                             new LoggingHandler(LogLevel.INFO),
                                    new SctpChatClientHandler());
                        }
                    });

            InetSocketAddress localAddress = SocketUtils.socketAddress(clientPrimaryAddress, clientPort);
            InetAddress localSecondaryAddress = SocketUtils.addressByName(clientSecondAddress);

            InetSocketAddress remoteAddress = SocketUtils.socketAddress(serverAddress, serverPort);

            // 绑定第一个address
            ChannelFuture bindFuture = b.bind(localAddress).sync();
            SctpChannel channel = (SctpChannel) bindFuture.channel();

            // 绑定第二个address
            channel.bindAddress(localSecondaryAddress).sync();
            ChannelFuture connectFuture = channel.connect(remoteAddress).sync();

            connectFuture.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
