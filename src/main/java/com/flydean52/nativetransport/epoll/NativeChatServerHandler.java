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
package com.flydean52.nativetransport.epoll;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * native server端的处理器
 */
@Sharable
@Slf4j
public class NativeChatServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("accepted channel: {}", ctx.channel());
        log.info("accepted channel parent: {}", ctx.channel().parent());
        // channel活跃
        ctx.write("Channel Active状态!\r\n");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // 如果读取到"再见"就关闭channel
        String response;
        // 判断是否关闭
        boolean close = false;
        if (request.isEmpty()) {
            response = "你说啥?\r\n";
        } else if ("再见".equalsIgnoreCase(request)) {
            response = "再见,我的朋友!\r\n";
            close = true;
        } else {
            response = "你是不是说: '" + request + "'?\r\n";
        }

        // 写入消息
        ChannelFuture future = ctx.write(response);
        // 添加CLOSE listener，用来关闭channel
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
