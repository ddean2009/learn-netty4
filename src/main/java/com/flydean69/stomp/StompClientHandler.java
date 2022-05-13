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
package com.flydean69.stomp;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.stomp.DefaultStompFrame;
import io.netty.handler.codec.stomp.StompCommand;
import io.netty.handler.codec.stomp.StompFrame;
import io.netty.handler.codec.stomp.StompHeaders;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StompClientHandler extends SimpleChannelInboundHandler<StompFrame> {

    private final String topic;
    private final String host;

    public StompClientHandler(String topic,String host){
        this.topic=topic;
        this.host=host;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        StompFrame connFrame = new DefaultStompFrame(StompCommand.CONNECT);
        connFrame.headers().set(StompHeaders.ACCEPT_VERSION, "1.2");
        connFrame.headers().set(StompHeaders.HOST, host);
        ctx.writeAndFlush(connFrame);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, StompFrame frame) throws Exception {
        log.info("read frame:{}",frame);
        switch (frame.command()) {
            case CONNECTED:
                StompFrame subscribeFrame = new DefaultStompFrame(StompCommand.SUBSCRIBE);
                subscribeFrame.headers().set(StompHeaders.DESTINATION, topic);
                subscribeFrame.headers().set(StompHeaders.RECEIPT, "10000");
                subscribeFrame.headers().set(StompHeaders.ID, "1");
                log.info("subscribeFrame:{}",subscribeFrame);
                ctx.writeAndFlush(subscribeFrame);
                break;
            case RECEIPT:
                String receiptHeader = frame.headers().getAsString(StompHeaders.RECEIPT_ID);
                if (receiptHeader.equals("10000")) {
                    StompFrame msgFrame = new DefaultStompFrame(StompCommand.SEND);
                    msgFrame.headers().set(StompHeaders.DESTINATION, topic);
                    msgFrame.content().writeBytes("hello world".getBytes());
                    log.info("msgFrame:{}",msgFrame);
                    ctx.writeAndFlush(msgFrame);
                } else if (receiptHeader.equals("10001")) {
                    log.info("disconnected");
                    ctx.close();
                } else {
                    throw new IllegalStateException("received: " + frame);
                }
                break;
            case MESSAGE:
                    StompFrame disconnFrame = new DefaultStompFrame(StompCommand.DISCONNECT);
                    disconnFrame.headers().set(StompHeaders.RECEIPT, "10001");
                    log.info("disconnFrame:{}",disconnFrame);
                    ctx.writeAndFlush(disconnFrame);
                break;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
