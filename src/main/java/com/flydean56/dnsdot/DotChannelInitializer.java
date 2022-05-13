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
