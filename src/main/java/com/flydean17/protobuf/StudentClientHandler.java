
package com.flydean17.protobuf;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StudentClientHandler extends SimpleChannelInboundHandler<Student> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel活跃
        //构建一个Student，并将其写入到channel中
        Student student= Student.newBuilder().setAge(22).setName("flydean").build();
        log.info("client发送消息{}",student);
        ctx.write(student);
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Student student) throws Exception {
        log.info("client收到消息{}",student);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
