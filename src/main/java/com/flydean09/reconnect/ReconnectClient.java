
package com.flydean09.reconnect;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;


/**
 * netty客户端
 */
public final class ReconnectClient {

    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
    // 在reconnect之前 Sleep 5 秒钟
    static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
    //如果在10秒中之内没有任何相应则重连
    private static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

    private static final ReconnectClientHandler handler = new ReconnectClientHandler();
    private static final Bootstrap bs = new Bootstrap();

    public static void main(String[] args) {
        EventLoopGroup group = new NioEventLoopGroup();
        bs.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(HOST, PORT)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new IdleStateHandler(READ_TIMEOUT, 0, 0), handler);
                    }
                });
        bs.connect();
    }

    static void connect() {
        bs.connect().addListener(future -> {
            if (future.cause() != null) {
                handler.startTime = -1;
                handler.println("建立连接失败: " + future.cause());
            }
        });
    }
}
