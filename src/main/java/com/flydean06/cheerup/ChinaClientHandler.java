
package com.flydean06.cheerup;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * 加油服务的客户端，
 */
@Slf4j
public class ChinaClientHandler extends ChannelInboundHandlerAdapter {

    private  ByteBuf message;

    /**
     * 客户端处理器
     */
    public ChinaClientHandler() {
        message = Unpooled.buffer(ChinaClient.SIZE);
        message.writeBytes("中国".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
        log.info("capacity:{},refCnt{}",message.capacity(),message.refCnt());
        message.retain();
        ctx.writeAndFlush(message);
//        ctx.writeAndFlush("中国");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        log.info("客户端收到消息:{}",((ByteBuf)msg).toString(StandardCharsets.UTF_8));
        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
        log.info("capacity:{},refCnt{}",message.capacity(),message.refCnt());

        log.info("可读字节:{},readerIndex:{}",message.readableBytes(),message.readerIndex());
        log.info("可写字节:{},writerIndex:{}",message.writableBytes(),message.writerIndex());
//        message = Unpooled.buffer(ChinaClient.SIZE);
//        message.writeBytes("中国".getBytes(StandardCharsets.UTF_8));
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
