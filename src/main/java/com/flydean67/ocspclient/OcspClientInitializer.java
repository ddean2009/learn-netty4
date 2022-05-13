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
package com.flydean67.ocspclient;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.ssl.ReferenceCountedOpenSslContext;
import io.netty.handler.ssl.ReferenceCountedOpenSslEngine;
import io.netty.handler.ssl.SslHandler;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class OcspClientInitializer extends ChannelInitializer {

    private ReferenceCountedOpenSslContext context;
    private String host;

    @Override
    protected void initChannel(Channel ch) throws Exception {
        SslHandler sslHandler = context.newHandler(ch.alloc());
        ReferenceCountedOpenSslEngine engine
                = (ReferenceCountedOpenSslEngine) sslHandler.engine();

        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(sslHandler);
        pipeline.addLast(new CustOcspClientHandler(engine));

        pipeline.addLast(new HttpClientCodec());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new OcspClientHandlerAdapter(host));
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ctx.fireChannelInactive();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
    }
}
