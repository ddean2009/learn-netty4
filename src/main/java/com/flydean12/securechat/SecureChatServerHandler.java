
package com.flydean12.securechat;

import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * server端的处理器
 */
@Sharable
@Slf4j
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {
    //一个全局共享的channel group
    static final ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // channel活跃
        ctx.write("Channel Active状态!\r\n");

        ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener(
                (GenericFutureListener<Future<Channel>>) future -> {
                    ctx.writeAndFlush(
                            "欢迎你: " + InetAddress.getLocalHost().getHostName() + " !\n");
                    ctx.writeAndFlush(
                            "从现在起，你的会话将使用: " +
                                    ctx.pipeline().get(SslHandler.class).engine().getSession().getCipherSuite() +
                                    " 进行加密保护.\n");
                    // 将新创建的channel添加到全局的channel group中，用于后续的消息广播
                    channels.add(ctx.channel());
                });
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, String message) throws Exception {
        // 将一个客户端的消息广播到所有的channel中
        for (Channel c: channels) {
            if (c != ctx.channel()) {
                c.writeAndFlush( ctx.channel().remoteAddress() + "说: " + message + '\n');
            } else {
                c.writeAndFlush("你是不是说: " + message + '\n');
            }
        }

        // 如果读取到"再见"就关闭channel
        String response;
        // 判断是否关闭
        boolean close = false;
        if (message.isEmpty()) {
            response = "你说啥?\r\n";
        } else if ("再见".equalsIgnoreCase(message)) {
            response = "再见,我的朋友!\r\n";
            close = true;
        } else {
            response = "你是不是说: '" + message + "'?\r\n";
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
