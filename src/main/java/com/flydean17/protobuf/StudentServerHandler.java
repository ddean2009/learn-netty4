
package com.flydean17.protobuf;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudentServerHandler extends SimpleChannelInboundHandler<Student> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student student) throws Exception {
        log.info("server收到消息{}",student);
        // 写入消息
        ChannelFuture future = ctx.write(student);
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
