
package com.flydean55.dnsudp;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.dns.*;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

@Slf4j
public final class Do53UdpClient {

    public void startDnsClient(String dnsServer, int dnsPort, String queryDomain) throws InterruptedException {
        InetSocketAddress addr = new InetSocketAddress(dnsServer, dnsPort);
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new Do53UdpChannelInitializer());

            final Channel ch = b.bind(0).sync().channel();

            int randomID = (int) (System.currentTimeMillis() / 1000);
            DnsQuery query = new DatagramDnsQuery(null, addr, randomID).setRecord(
                    DnsSection.QUESTION,
                    new DefaultDnsQuestion(queryDomain, DnsRecordType.A));
            ch.writeAndFlush(query).sync();
            boolean result = ch.closeFuture().await(10, TimeUnit.SECONDS);
            if (!result) {
                log.error("DNS查询失败");
                ch.close().sync();
            }
        } finally {
            group.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws Exception {

        Do53UdpClient client = new Do53UdpClient();
        final String dnsServer = "223.5.5.5";
        final int dnsPort = 53;
        final String queryDomain = "www.flydean.com";
        client.startDnsClient(dnsServer, dnsPort, queryDomain);
    }

}
