
package com.flydean65.mqttclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public final class MqttClient {

    public static void main(String[] args) throws Exception {
        String brokerHost = "127.0.0.1";
        int hostPort = 1883;
        String clientId = "clientId";
        String userName = "jack";
        String password = "ma";
        startClient(brokerHost, hostPort, clientId, userName, password);
    }

    private static void startClient(String brokerHost, int hostPort, String clientId, String userName, String password) throws InterruptedException {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.handler(new MqttClientChannelInitializer(clientId, userName, password));

            ChannelFuture f = b.connect(brokerHost, hostPort).sync();
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

}
