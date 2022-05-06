
package com.flydean69.stomp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.stomp.StompSubframeAggregator;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public final class StompClient {

    public static void main(String[] args) throws Exception {
        String host="127.0.0.1";
        int port = 61613;
        String topic = "stomp.topic.example";
        startClient(host, port, topic);
    }

    private static void startClient(String host, int port, String topic) throws InterruptedException {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();
                    pipeline.addLast("decoder", new StompSubframeDecoder());
                    pipeline.addLast("encoder", new StompSubframeEncoder());
                    pipeline.addLast("aggregator", new StompSubframeAggregator(100000));
                    pipeline.addLast("handler", new StompClientHandler(topic,host));
                }
            });

            b.connect(host, port).sync().channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
