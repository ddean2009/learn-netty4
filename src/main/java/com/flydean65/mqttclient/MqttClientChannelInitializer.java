package com.flydean65.mqttclient;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.mqtt.MqttDecoder;
import io.netty.handler.codec.mqtt.MqttEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.AllArgsConstructor;

import java.util.concurrent.TimeUnit;

@AllArgsConstructor
class MqttClientChannelInitializer extends ChannelInitializer<SocketChannel> {
    private final String clientId;
    private final String userName;
    private final String password;

    protected void initChannel(SocketChannel ch) {
        ch.pipeline().addLast( MqttEncoder.INSTANCE);
        ch.pipeline().addLast(new MqttDecoder());
        ch.pipeline().addLast(new IdleStateHandler(0, 30, 0, TimeUnit.SECONDS));
        ch.pipeline().addLast(new MqttClientHandler(clientId, userName, password));
    }
}
