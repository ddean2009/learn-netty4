
package com.flydean44.tfo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * TFO client 处理器
 */
@Slf4j
public class TFOClientHandler extends ChannelInboundHandlerAdapter {

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
