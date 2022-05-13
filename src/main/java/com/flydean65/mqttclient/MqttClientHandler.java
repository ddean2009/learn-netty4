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
package com.flydean65.mqttclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.mqtt.*;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MqttClientHandler extends ChannelInboundHandlerAdapter {

    private final String clientId;
    private final String userName;
    private final byte[] password;

    public MqttClientHandler(String clientId, String userName, String password) {
        this.clientId = clientId;
        this.userName = userName;
        this.password = password.getBytes();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("channel read:{}", msg);
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        MqttFixedHeader connectFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNECT, false, MqttQoS.AT_MOST_ONCE, false, 0);
        MqttConnectVariableHeader connectVariableHeader =
                new MqttConnectVariableHeader(MqttVersion.MQTT_3_1_1.protocolName(), MqttVersion.MQTT_3_1_1.protocolLevel(), true, true, false,
                                              0, false, false, 20, MqttProperties.NO_PROPERTIES);
        MqttConnectPayload connectPayload = new MqttConnectPayload(clientId,
                MqttProperties.NO_PROPERTIES,
                null,
                null,
                userName,
                password);
        MqttConnectMessage connectMessage =
                new MqttConnectMessage(connectFixedHeader, connectVariableHeader, connectPayload);
        log.info("发送connect消息");
        ctx.writeAndFlush(connectMessage);
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            MqttMessage pingReqMessage = MqttMessage.PINGREQ;
            log.info("发送pingReq消息");
            ctx.writeAndFlush(pingReqMessage);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("出现异常",cause);
        ctx.close();
    }
}
