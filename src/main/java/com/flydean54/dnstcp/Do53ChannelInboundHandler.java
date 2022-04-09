package com.flydean54.dnstcp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Do53ChannelInboundHandler extends SimpleChannelInboundHandler<DefaultDnsResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DefaultDnsResponse msg) {
        try {
            readMsg(msg);
        } finally {
            ctx.close();
        }
    }

    private static void readMsg(DefaultDnsResponse msg) {
        if (msg.count(DnsSection.QUESTION) > 0) {
            DnsQuestion question = msg.recordAt(DnsSection.QUESTION, 0);
            log.info("question is :{}",question);
        }
        int i = 0, count = msg.count(DnsSection.ANSWER);
        while (i < count) {
            DnsRecord record = msg.recordAt(DnsSection.ANSWER, i);
            //A记录用来指定主机名或者域名对应的IP地址
            if (record.type() == DnsRecordType.A) {
                DnsRawRecord raw = (DnsRawRecord) record;
                log.info("ip address is: {}",NetUtil.bytesToIpAddress(ByteBufUtil.getBytes(raw.content())));
            }
            i++;
        }
    }
}
