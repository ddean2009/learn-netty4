
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
