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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 处理 Http2StreamFrame 响应.
 */
@Slf4j
public final class Http2ClientStreamFrameHandler extends SimpleChannelInboundHandler<Http2StreamFrame> {

    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2StreamFrame msg) throws Exception {
        log.info("接收 Http2StreamFrame: {}",msg);

        // 判断stream是否endStream
        if (msg instanceof Http2DataFrame && ((Http2DataFrame) msg).isEndStream()) {
            log.info("Http2DataFrame接收到endStream flag");
            log.info("msg:{}",((Http2DataFrame) msg).content().getCharSequence(0,33, CharsetUtil.UTF_8));
            latch.countDown();
        } else if (msg instanceof Http2HeadersFrame && ((Http2HeadersFrame) msg).isEndStream()) {
            log.info("Http2HeadersFrame接收到endStream flag");
            latch.countDown();
        }
    }

    /**
     * 等待latch countDown或者超时5秒钟
     * @return true 表示成功接收到了一个endStream
     */
    public boolean responseSuccessfullyCompleted() {
        try {
            return latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.error("Latch exception: {}" + ie.getMessage());
            return false;
        }
    }

}
