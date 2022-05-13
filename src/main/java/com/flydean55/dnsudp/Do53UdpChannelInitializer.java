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

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.handler.codec.dns.DatagramDnsQueryEncoder;
import io.netty.handler.codec.dns.DatagramDnsResponseDecoder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
class Do53UdpChannelInitializer extends ChannelInitializer<DatagramChannel> {
    @Override
    protected void initChannel(DatagramChannel ch) throws Exception {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new DatagramDnsQueryEncoder())
                .addLast(new DatagramDnsResponseDecoder())
                .addLast(new Do53UdpChannelInboundHandler());
    }
}
