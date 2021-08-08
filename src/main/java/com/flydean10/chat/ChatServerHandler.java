
package com.flydean10.chat;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * server端的处理器
 */
@Sharable
@Slf4j
public class ChatServerHandler extends SimpleChannelInboundHandler<String> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel活跃
        ctx.write("Channel Active状态!");
        ctx.flush();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String request) throws Exception {
        // 如果读取到"再见"就关闭channel
        String response;
        // 判断是否关闭
        boolean close = false;
        if (request.isEmpty()) {
            response = "你说啥?\r\n";
        } else if ("再见".equalsIgnoreCase(request)) {
            response = "再见,我的朋友!\r\n";
            close = true;
        } else {
            response = "你是不是说: '" + request + "'?\r\n";
        }

        // 写入消息
        ChannelFuture future = ctx.write(response);

        // 添加CLOSE listener，用来关闭channel
        if (close) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
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
