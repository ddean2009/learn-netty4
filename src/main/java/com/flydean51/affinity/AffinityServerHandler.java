package com.flydean51.affinity;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author wayne
 * @version AffinityServerHandler,  2021/8/1
 */
@Slf4j
public class AffinityServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
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
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }
}
