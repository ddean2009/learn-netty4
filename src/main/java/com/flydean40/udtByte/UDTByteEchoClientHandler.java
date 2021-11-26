
package com.flydean40.udtByte;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.udt.nio.NioUdtProvider;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 客户端处理器
 */
@Slf4j
public class UDTByteEchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {

    private final ByteBuf message;

    public UDTByteEchoClientHandler() {
        super(false);
        message = Unpooled.buffer(UDTByteEchoClient.SIZE);
        message.writeBytes("www.flydean.com".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        log.info("channel active " + NioUdtProvider.socketUDT(ctx.channel()).toStringOptions());
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) {
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
