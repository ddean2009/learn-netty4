
package com.flydean42.udtRendezvous;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.UdtMessage;
import io.netty.channel.udt.nio.NioUdtProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

@Slf4j
public class UDTMsgHandler extends SimpleChannelInboundHandler<UdtMessage> {

    private final UdtMessage message;

    public UDTMsgHandler(final int messageSize) {
        super(false);
        final ByteBuf byteBuf = Unpooled.buffer(messageSize);
        byteBuf.writeBytes("www.flydean.com".getBytes(StandardCharsets.UTF_8));
        message = new UdtMessage(byteBuf);
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        log.info("ECHO active {}", NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, UdtMessage message) {
        ctx.write(message);
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
