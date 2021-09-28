

package com.flydean34.http2images;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 支持http1 的image服务器
 */
public final class ImageHttp1Server {

    public static final int PORT = 8000;
    private static final int MAX_CONTENT_LENGTH = 1024 * 100;

    private final EventLoopGroup group;

    public ImageHttp1Server(EventLoopGroup eventLoopGroup) {
        group = eventLoopGroup;
    }

    public ChannelFuture start() throws Exception {
        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);

        b.group(group).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
        .childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch){
                ch.pipeline().addLast(new HttpRequestDecoder(),
                                      new HttpResponseEncoder(),
                                      new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                                      new Http1RequestHandler());
            }
        });

        Channel ch = b.bind(PORT).sync().channel();
        return ch.closeFuture();
    }
}
