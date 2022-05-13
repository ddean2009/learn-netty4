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
package com.flydean19.httpclientrequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClientRequestClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            log.info("STATUS: {}" , response.status());
            log.info("VERSION: {}" , response.protocolVersion());

            if (!response.headers().isEmpty()) {
                for (CharSequence name: response.headers().names()) {
                    for (CharSequence value: response.headers().getAll(name)) {
                        log.info("HEADER: {}={}" ,name , value);
                    }
                }
            }

            if (HttpUtil.isTransferEncodingChunked(response)) {
                log.info("CHUNKED CONTENT {");
            } else {
                log.info("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent content = (HttpContent) msg;

            log.info(content.content().toString(CharsetUtil.UTF_8));

            if (content instanceof LastHttpContent) {
                log.info("} END OF CONTENT");
                ctx.close();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
