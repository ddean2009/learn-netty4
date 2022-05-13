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
package com.flydean42.udtRendezvous;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.nio.NioUdtProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class UDTByteHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final ByteBuf message;

    public UDTByteHandler(final int messageSize) {
        super(false);
        message = Unpooled.buffer(messageSize);
        message.writeBytes("www.flydean.com".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("ECHO active {}" ,NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf buf) {
        ctx.write(buf);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(),cause);
        ctx.close();
    }
}
