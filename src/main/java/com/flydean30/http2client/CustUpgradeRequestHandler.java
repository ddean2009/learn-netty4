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
package com.flydean30.http2client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;

/**
 * upgrade处理器
 */
final class CustUpgradeRequestHandler extends ChannelInboundHandlerAdapter {

    private final CustHttp2ClientInitializer custHttp2ClientInitializer;

    public CustUpgradeRequestHandler(CustHttp2ClientInitializer custHttp2ClientInitializer) {
        this.custHttp2ClientInitializer = custHttp2ClientInitializer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        DefaultFullHttpRequest upgradeRequest =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);

        // 设置upgradeRequest的host地址
        InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostString = remote.getHostString();
        if (hostString == null) {
            hostString = remote.getAddress().getHostAddress();
        }
        upgradeRequest.headers().set(HttpHeaderNames.HOST, hostString + ':' + remote.getPort());

        ctx.writeAndFlush(upgradeRequest);
        ctx.fireChannelActive();
        // 升级完毕，从pipeline中删除
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(custHttp2ClientInitializer.settingsHandler(), custHttp2ClientInitializer.responseHandler());
    }
}
