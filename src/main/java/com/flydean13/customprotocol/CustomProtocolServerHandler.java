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
package com.flydean13.customprotocol;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;

/**
 * 自定义协议处理器
 */
@Slf4j
public class CustomProtocolServerHandler extends SimpleChannelInboundHandler<BigInteger> {

    private int count = 0;
    private BigInteger result=BigInteger.valueOf(1);

    @Override
    public void channelRead0(ChannelHandlerContext ctx, BigInteger msg) throws Exception {
        // 将接收到的msg乘以2，然后返回给客户端
        count++;
        result = result.multiply(msg);
        ctx.writeAndFlush(result);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("2的{}次方是{}",count,result);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
