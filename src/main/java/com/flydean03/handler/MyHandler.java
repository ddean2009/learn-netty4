
package com.flydean03.handler;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * Handles a server-side channel.
 */
@Slf4j
public class MyHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 对消息进行处理
        ByteBuf in = (ByteBuf) msg;
        try {
            log.info("收到消息:{}",in.toString(io.netty.util.CharsetUtil.US_ASCII));
        }finally {
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //异常处理
        cause.printStackTrace();
        ctx.close();
    }
}
