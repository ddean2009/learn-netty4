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

package com.flydean34.http2images;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapter;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;

/**
 * protocol协商处理器
 */
public class CustProtocolNegotiationHandler extends ApplicationProtocolNegotiationHandler {

    private static final int MAX_CONTENT_LENGTH = 1024 * 100;

    protected CustProtocolNegotiationHandler() {
        super(ApplicationProtocolNames.HTTP_1_1);
    }

    @Override
    protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
        if (ApplicationProtocolNames.HTTP_2.equals(protocol)) {
            configureHttp2(ctx);
            return;
        }

        if (ApplicationProtocolNames.HTTP_1_1.equals(protocol)) {
            configureHttp1(ctx);
            return;
        }

        throw new IllegalStateException("未知协议: " + protocol);
    }

    private static void configureHttp2(ChannelHandlerContext ctx) {
        DefaultHttp2Connection connection = new DefaultHttp2Connection(true);
        InboundHttp2ToHttpAdapter listener = new InboundHttp2ToHttpAdapterBuilder(connection)
                .propagateSettings(true).validateHttpHeaders(false)
                .maxContentLength(MAX_CONTENT_LENGTH).build();

        ctx.pipeline().addLast(new HttpToHttp2ConnectionHandlerBuilder()
                .frameListener(listener)
                .connection(connection).build());

        ctx.pipeline().addLast(new Http2RequestHandler());
    }

    private static void configureHttp1(ChannelHandlerContext ctx) {
        ctx.pipeline().addLast(new HttpServerCodec(),
                               new HttpObjectAggregator(MAX_CONTENT_LENGTH),
                               new Http1RequestHandler());
    }
}
