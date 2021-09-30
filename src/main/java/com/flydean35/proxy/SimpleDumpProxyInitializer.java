
package com.flydean35.proxy;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class SimpleDumpProxyInitializer extends ChannelInitializer<SocketChannel> {

    private final String remoteHost;
    private final int remotePort;

    public SimpleDumpProxyInitializer(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void initChannel(SocketChannel ch) {
        ch.pipeline().addLast(
                new LoggingHandler(LogLevel.INFO),
                new SimpleDumpProxyInboundHandler(remoteHost, remotePort));
    }
}
