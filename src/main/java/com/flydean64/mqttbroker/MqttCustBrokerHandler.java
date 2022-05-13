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
package com.flydean64.mqttbroker;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Sharable
public final class MqttCustBrokerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        MqttMessage mqttMessage = (MqttMessage) msg;
        log.info("channel read:{}", mqttMessage);

        switch (mqttMessage.fixedHeader().messageType()) {
            case CONNECT:
                MqttFixedHeader connectFixedHeader =
                        new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 0);
                MqttConnAckVariableHeader connectAckVariableHeader =
                        new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED, false);
                MqttConnAckMessage connAck = new MqttConnAckMessage(connectFixedHeader, connectAckVariableHeader);
                ctx.writeAndFlush(connAck);
                break;
            case PINGREQ:
                MqttMessage pingResp = MqttMessage.PINGRESP;
                ctx.writeAndFlush(pingResp);
                break;
            case DISCONNECT:
                ctx.close();
                break;
            default:
                log.info("未知消息类型:{}", msg);
                ReferenceCountUtil.release(msg);
                ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        log.info("idle state");
        if (evt instanceof IdleStateEvent && IdleState.READER_IDLE == ((IdleStateEvent) evt).state()) {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("消息异常", cause);
        ctx.close();
    }
}
