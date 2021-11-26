
package com.flydean41.udtMessage;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;
import io.netty.channel.udt.nio.NioUdtProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * UDT message客户端处理器
 */
@Slf4j
public class MsgEchoClientHandler extends SimpleChannelInboundHandler<UdtMessage> {

    private final UdtMessage message;

    public MsgEchoClientHandler() {
        super(false);
        final ByteBuf byteBuf = Unpooled.buffer(MsgEchoClient.SIZE);
        byteBuf.writeBytes("www.flydean.com".getBytes(StandardCharsets.UTF_8));
        message = new UdtMessage(byteBuf);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        log.info("channel active {}", NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UdtMessage msg) {
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
