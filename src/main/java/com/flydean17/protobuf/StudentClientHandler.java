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
package com.flydean17.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudentClientHandler extends SimpleChannelInboundHandler<Student> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel活跃
        //构建一个Student，并将其写入到channel中
        Student student= Student.newBuilder().setAge(22).setName("flydean").build();
        log.info("client发送消息{}",student);
        ctx.write(student);
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student student) throws Exception {
        log.info("client收到消息{}",student);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
