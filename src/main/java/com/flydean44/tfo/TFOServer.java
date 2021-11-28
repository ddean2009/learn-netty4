package com.flydean44.tfo;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.AllArgsConstructor;

/**
 * @author wayne
 * @version TFOServer,  2021/8/1
 */
@AllArgsConstructor
public class TFOServer {

    private final int port;

    public void start() throws InterruptedException {
        //建立两个EventloopGroup用来处理连接和消息
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new TFOServerHandler());
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .option(ChannelOption.TCP_FASTOPEN, 50)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            // 绑定端口并开始接收连接
            ChannelFuture f = b.bind(port).sync();
            // 等待server socket关闭
            f.channel().closeFuture().sync();
        } finally {
            //关闭group
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int port=8000;
        new TFOServer(port).start();
    }

}
