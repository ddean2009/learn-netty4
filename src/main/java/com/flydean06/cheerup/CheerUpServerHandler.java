
package com.flydean06.cheerup;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 加油服务器的处理器
 */
@Slf4j
@Sharable
public class CheerUpServerHandler extends ChannelInboundHandlerAdapter {

    private  ByteBuf message;

    public CheerUpServerHandler(){
        message = Unpooled.buffer(CheerUpServer.SIZE);
        message.writeBytes("加油!".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("收到消息:{}",msg);
        log.info("服务器端收到消息:{}",((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());

//        message = Unpooled.buffer(CheerUpServer.SIZE);
//        message.writeBytes("加油!".getBytes(StandardCharsets.UTF_8));
        message.retain();
        ctx.writeAndFlush(message);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
