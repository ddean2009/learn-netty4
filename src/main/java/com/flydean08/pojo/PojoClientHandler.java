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
package com.flydean08.pojo;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * client端处理器，
 */
@Slf4j
public class PojoClientHandler extends ChannelInboundHandlerAdapter {

    private final List<Integer> firstMessage;

    /**
     * 初始化handler
     */
    public PojoClientHandler() {
        firstMessage = new ArrayList<>(PojoClient.SIZE);
        for (int i = 0; i < PojoClient.SIZE; i ++) {
            firstMessage.add(i);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 在channel active的时候发送消息
        ChannelFuture future = ctx.writeAndFlush("中国");
        // 将ChannelFuture中的Throwable转发到ChannelPipeline中。
        future.addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将消息写回channel
        log.info("客户端收到对象:{}",msg);
        ctx.write(msg);
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
