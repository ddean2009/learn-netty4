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
package com.flydean21.httpupload;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传文件处理器
 */
@Slf4j
public class HttpUploadClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private boolean readingChunks;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            log.info("STATUS: " + response.status());
            log.info("VERSION: " + response.protocolVersion());

            if (!response.headers().isEmpty()) {
                for (CharSequence name : response.headers().names()) {
                    for (CharSequence value : response.headers().getAll(name)) {
                        log.info("HEADER: " + name + " = " + value);
                    }
                }
            }

            if (response.status().code() == 200 && HttpUtil.isTransferEncodingChunked(response)) {
                readingChunks = true;
                log.info("CHUNKED CONTENT {");
            } else {
                log.info("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;

            if (chunk instanceof LastHttpContent) {
                log.info(chunk.content().toString(CharsetUtil.UTF_8));
                if (readingChunks) {
                    log.info("} END OF CHUNKED CONTENT");
                } else {
                    log.info("} END OF CONTENT");
                }
                readingChunks = false;
            } else {
                log.info(chunk.content().toString(CharsetUtil.UTF_8));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.channel().close();
    }
}
