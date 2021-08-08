
package com.flydean01.first;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * Handles a client-side channel.
 */
@Slf4j
public class FirstClientHandler extends ChannelInboundHandlerAdapter {

    private ByteBuf content;
    private ChannelHandlerContext ctx;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        content = ctx.alloc().directBuffer(256).writeBytes("Hello flydean.com".getBytes(StandardCharsets.UTF_8));
        // 发送消息
        sayHello();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }

    private void sayHello() {
        // 向服务器输出消息
        ctx.writeAndFlush(content.retain());
    }
}
