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

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 处理 Http2Setting
 */
@Slf4j
public class CustHttp2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
    private final ChannelPromise promise;

    public CustHttp2SettingsHandler(ChannelPromise promise) {
        this.promise = promise;
    }

    /**
     * 等待设置完毕
     */
    public void awaitSettings(long timeout, TimeUnit unit){
        if (!promise.awaitUninterruptibly(timeout, unit)) {
            throw new IllegalStateException("设置时间超时");
        }
        if (!promise.isSuccess()) {
            throw new RuntimeException(promise.cause());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
        log.info("接收到Http2Settings消息:{}",msg);
        promise.setSuccess();
        //处理完毕，删除handler
        ctx.pipeline().remove(this);
    }
}
