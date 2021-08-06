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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 加油服务的客户端，
 */
@Slf4j
public class ChinaClientHandler extends ChannelInboundHandlerAdapter {

    private  ByteBuf message;

    /**
     * 客户端处理器
     */
    public ChinaClientHandler() {
        message = Unpooled.buffer(ChinaClient.SIZE);
        message.writeBytes("中国".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
        log.info("capacity:{},refCnt{}",message.capacity(),message.refCnt());
        message.retain();
        ctx.writeAndFlush(message);
//        ctx.writeAndFlush("中国");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("客户端收到消息:{}",((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
        log.info("capacity:{},refCnt{}",message.capacity(),message.refCnt());

        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
//        message = Unpooled.buffer(ChinaClient.SIZE);
//        message.writeBytes("中国".getBytes(StandardCharsets.UTF_8));
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
