
package com.flydean41.udtMessage;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.udt.nio.NioUdtProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * UDT message 服务器端处理器
 */
@Sharable
@Slf4j
public class MsgEchoServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        log.info("ECHO active {}" , NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ctx.write(msg);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(final ChannelHandlerContext ctx, final Throwable cause) {
        log.error(cause.getMessage(),cause);
        ctx.close();
    }
}
