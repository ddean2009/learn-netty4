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
package com.flydean32.http2framecodecclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.ssl.SslContext;
import lombok.extern.slf4j.Slf4j;

/**
 * 通过添加Http2FrameCodec和Http2MultiplexHandler，让客户端支持HTTP/2 frames.
 */
@Slf4j
public final class Http2ClientFrameInitializer extends ChannelInitializer<Channel> {

    private final SslContext sslCtx;

    public Http2ClientFrameInitializer(SslContext sslCtx) {
        this.sslCtx = sslCtx;
    }

    @Override
    protected void initChannel(Channel ch) throws Exception {
        if (sslCtx != null) {
            ch.pipeline().addFirst(sslCtx.newHandler(ch.alloc()));
        }

        Http2FrameCodec http2FrameCodec = Http2FrameCodecBuilder.forClient()
            .initialSettings(Http2Settings.defaultSettings())
            .build();
        ch.pipeline().addLast(http2FrameCodec);
        ch.pipeline().addLast(new Http2MultiplexHandler(new SimpleChannelInboundHandler() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
                // 处理inbound streams
                log.info("Http2MultiplexHandler接收到消息: {}",msg);
            }
        }));
    }

}
