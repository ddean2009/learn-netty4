
package com.flydean35.proxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleDumpProxyInboundHandler extends ChannelInboundHandlerAdapter {

    private final String remoteHost;
    private final int remotePort;

    // outboundChannel和inboundChannel使用同一个eventloop
    private Channel outboundChannel;

    public SimpleDumpProxyInboundHandler(String remoteHost, int remotePort) {
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        final Channel inboundChannel = ctx.channel();

        // 开启outbound连接
        Bootstrap b = new Bootstrap();
        b.group(inboundChannel.eventLoop())
         .channel(ctx.channel().getClass())
         .handler(new SimpleDumpProxyOutboundHandler(inboundChannel))
         .option(ChannelOption.AUTO_READ, false);
        ChannelFuture f = b.connect(remoteHost, remotePort);
        outboundChannel = f.channel();
        f.addListener(future -> {
            if (future.isSuccess()) {
                // 连接建立完毕，读取inbound数据
                inboundChannel.read();
            } else {
                // 关闭inbound channel
                inboundChannel.close();
            }
        });
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        // 将inboundChannel中的消息读取，并写入到outboundChannel
        if (outboundChannel.isActive()) {
            outboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    // flush成功，读取下一个消息
                    ctx.channel().read();
                } else {
                    future.channel().close();
                }
            });
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (outboundChannel != null) {
            closeOnFlush(outboundChannel);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        closeOnFlush(ctx.channel());
    }


    static void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
