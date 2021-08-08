
package com.flydean09.reconnect;

import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 重连handler
 */
@Sharable
@Slf4j
public class ReconnectClientHandler extends SimpleChannelInboundHandler<Object> {

    long startTime = -1;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        if (startTime < 0) {
            startTime = System.currentTimeMillis();
        }
        println("连接到: " + ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 读取消息
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        if (!(evt instanceof IdleStateEvent)) {
            return;
        }
        IdleStateEvent e = (IdleStateEvent) evt;
        if (e.state() == IdleState.READER_IDLE) {
            // 在Idle状态
            println("Idle状态，关闭连接");
            ctx.close();
        }
    }

    @Override
    public void channelInactive(final ChannelHandlerContext ctx) {
        println("连接断开:" + ctx.channel().remoteAddress());
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        println("sleep:" + ReconnectClient.RECONNECT_DELAY + 's');

        ctx.channel().eventLoop().schedule(() -> {
            println("重连接: " + ReconnectClient.HOST + ':' + ReconnectClient.PORT);
            ReconnectClient.connect();
        }, ReconnectClient.RECONNECT_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }

    void println(String msg) {
        if (startTime < 0) {
            log.error("服务下线:{}",msg);
        } else {
            log.error("服务运行时间:{},{}",(System.currentTimeMillis() - startTime) / 1000, msg);
        }
    }
}
