
package com.flydean35.proxy;

import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SimpleDumpProxyOutboundHandler extends ChannelInboundHandlerAdapter {

    private final Channel inboundChannel;

    public SimpleDumpProxyOutboundHandler(Channel inboundChannel) {
        this.inboundChannel = inboundChannel;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.read();
    }

    @Override
    public void channelRead(final ChannelHandlerContext ctx, Object msg) {
        // 将outboundChannel中的消息读取，并写入到inboundChannel中
        inboundChannel.writeAndFlush(msg).addListener((ChannelFutureListener) future -> {
            if (future.isSuccess()) {
                ctx.channel().read();
            } else {
                future.channel().close();
            }
        });
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        SimpleDumpProxyInboundHandler.closeOnFlush(inboundChannel);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        SimpleDumpProxyInboundHandler.closeOnFlush(ctx.channel());
    }
}
