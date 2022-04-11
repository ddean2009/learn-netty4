package com.flydean57.dnsserver;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Do53ServerInboundHandler extends SimpleChannelInboundHandler<DnsQuery> {

    private static final byte[] QUERY_RESULT = new byte[]{46, 53, 107, 110};

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                DnsQuery msg) throws Exception {
        DnsQuestion question = msg.recordAt(DnsSection.QUESTION);
        log.info("Query is: {}", question);
        ctx.writeAndFlush(newResponse(msg, question, 1000, QUERY_RESULT));
    }

    private DefaultDnsResponse newResponse(DnsQuery query,
                                           DnsQuestion question,
                                           long ttl, byte[]... addresses) {
        DefaultDnsResponse response = new DefaultDnsResponse(query.id());
        response.addRecord(DnsSection.QUESTION, question);

        for (byte[] address : addresses) {
            DefaultDnsRawRecord queryAnswer = new DefaultDnsRawRecord(
                    question.name(),
                    DnsRecordType.A, ttl, Unpooled.wrappedBuffer(address));
            response.addRecord(DnsSection.ANSWER, queryAnswer);
        }
        return response;
    }
}
