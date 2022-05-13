/*
 * Copyright 2022 learn-netty4 Project
 *
 * The learn-netty4 Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.flydean55.dnsudp;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.dns.*;
import io.netty.util.NetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Do53UdpChannelInboundHandler extends SimpleChannelInboundHandler<DatagramDnsResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramDnsResponse msg) {
        try {
            readMsg(msg);
        } finally {
            ctx.close();
        }
    }

    private static void readMsg(DatagramDnsResponse msg) {
        if (msg.count(DnsSection.QUESTION) > 0) {
            DnsQuestion question = msg.recordAt(DnsSection.QUESTION, 0);
            log.info("question is :{}", question);
        }
        int i = 0, count = msg.count(DnsSection.ANSWER);
        while (i < count) {
            DnsRecord record = msg.recordAt(DnsSection.ANSWER, i);
            if (record.type() == DnsRecordType.A) {
                //A记录用来指定主机名或者域名对应的IP地址
                DnsRawRecord raw = (DnsRawRecord) record;
                log.info("ip address is: {}",NetUtil.bytesToIpAddress(ByteBufUtil.getBytes(raw.content())));
            }
            i++;
        }
    }
}
