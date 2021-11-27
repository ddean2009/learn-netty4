
package com.flydean42.udtRendezvous;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.udt.UdtChannel;
import io.netty.channel.udt.nio.NioUdtProvider;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.concurrent.DefaultThreadFactory;

import java.net.SocketAddress;
import java.util.concurrent.ThreadFactory;

public class UDTByteRendezvousBase {

    protected final int messageSize;
    protected final SocketAddress myAddress;
    protected final SocketAddress peerAddress;

    public UDTByteRendezvousBase(int messageSize, SocketAddress myAddress, SocketAddress peerAddress) {
        this.messageSize = messageSize;
        this.myAddress = myAddress;
        this.peerAddress = peerAddress;
    }

    public void run() throws Exception {
        final ThreadFactory connectFactory = new DefaultThreadFactory("rendezvous");
        final NioEventLoopGroup connectGroup = new NioEventLoopGroup(1,
                connectFactory, NioUdtProvider.BYTE_PROVIDER);
        try {
            final Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(connectGroup)
                    .channelFactory(NioUdtProvider.BYTE_RENDEZVOUS)
                    .handler(new ChannelInitializer<UdtChannel>() {
                        @Override
                        protected void initChannel(UdtChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new LoggingHandler(LogLevel.INFO),
                                    new UDTByteHandler(messageSize));
                        }
                    });
            final ChannelFuture future = bootstrap.connect(peerAddress, myAddress).sync();
            future.channel().closeFuture().sync();
        } finally {
            connectGroup.shutdownGracefully();
        }
    }
}
