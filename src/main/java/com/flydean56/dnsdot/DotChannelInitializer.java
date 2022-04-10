package com.flydean56.dnsdot;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.dns.TcpDnsQueryEncoder;
import io.netty.handler.codec.dns.TcpDnsResponseDecoder;
import io.netty.handler.ssl.SslContext;

class DotChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final SslContext sslContext;
    private final String dnsServer;
    private final int dnsPort;

    public DotChannelInitializer(SslContext sslContext, String dnsServer, int dnsPort) {
        this.sslContext = sslContext;
        this.dnsServer = dnsServer;
        this.dnsPort = dnsPort;
    }

    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(sslContext.newHandler(ch.alloc(), dnsServer, dnsPort))
                .addLast(new TcpDnsQueryEncoder())
                .addLast(new TcpDnsResponseDecoder())
                .addLast(new DotChannelInboundHandler());
    }

}
