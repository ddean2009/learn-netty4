
package com.flydean08.pojo;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

import static io.netty.channel.ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE;

/**
 * client端处理器，
 */
@Slf4j
public class PojoClientHandler extends ChannelInboundHandlerAdapter {

    private final List<Integer> firstMessage;

    /**
     * 初始化handler
     */
    public PojoClientHandler() {
        firstMessage = new ArrayList<>(PojoClient.SIZE);
        for (int i = 0; i < PojoClient.SIZE; i ++) {
            firstMessage.add(i);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 在channel active的时候发送消息
        ChannelFuture future = ctx.writeAndFlush("中国");
        // 将ChannelFuture中的Throwable转发到ChannelPipeline中。
        future.addListener(FIRE_EXCEPTION_ON_FAILURE);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        // 将消息写回channel
        log.info("客户端收到对象:{}",msg);
        ctx.write(msg);
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
