
package com.flydean67.ocspclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.OpenSsl;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLException;

/**
 * 一个HTTPS客户端，向支持OCSP stapling的服务器请求消息，并校验OCSP响应的正确性
 */
@Slf4j
public class OcspClient {

    public static void main(String[] args) throws Exception {
        String host = "www.microsoft.com";
        startClient(host);
    }

    private static void startClient(String host) throws SSLException, InterruptedException {
        if (!OpenSsl.isAvailable()) {
            log.error("客户端必须支持SSL");
        }
        if (!OpenSsl.isOcspSupported()) {
            log.error("客户端必须支持ocsp");
        }

        ReferenceCountedOpenSslContext context
                = (ReferenceCountedOpenSslContext) SslContextBuilder.forClient()
                .sslProvider(SslProvider.OPENSSL)
                .enableOcsp(true)
                .build();

        try {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap b = new Bootstrap()
                        .channel(NioSocketChannel.class)
                        .group(group)
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5 * 1000)
                        .handler(new OcspClientInitializer(context, host));

                ChannelFuture f = b.connect(host, 443).sync();
                f.channel().closeFuture().sync();
            } finally {
                group.shutdownGracefully();
            }
        } finally {
            context.release();
        }
    }
}
