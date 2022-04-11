package com.flydean57.dnsserver;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.dns.*;

class Do53ServerChannelInitializer extends ChannelInitializer<Channel> {
    @Override
    protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
                new TcpDnsQueryDecoder(),
                new TcpDnsResponseEncoder(),
                new Do53ServerInboundHandler());
    }
}
