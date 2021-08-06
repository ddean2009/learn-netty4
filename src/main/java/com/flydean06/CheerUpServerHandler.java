/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
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
package com.flydean06;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 加油服务器的处理器
 */
@Slf4j
@Sharable
public class CheerUpServerHandler extends ChannelInboundHandlerAdapter {

    private  ByteBuf message;

    public CheerUpServerHandler(){
        message = Unpooled.buffer(CheerUpServer.SIZE);
        message.writeBytes("加油!".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("收到消息:{}",msg);
        log.info("服务器端收到消息:{}",((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());

//        message = Unpooled.buffer(CheerUpServer.SIZE);
//        message.writeBytes("加油!".getBytes(StandardCharsets.UTF_8));
        message.retain();
        ctx.writeAndFlush(message);
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
