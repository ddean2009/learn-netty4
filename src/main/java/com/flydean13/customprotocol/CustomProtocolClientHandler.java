
package com.flydean13.customprotocol;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.math.BigInteger;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 客户端的handler
 */
@Slf4j
public class CustomProtocolClientHandler extends SimpleChannelInboundHandler<BigInteger> {

    private ChannelHandlerContext ctx;
    private int receivedMessages;
    private int next = 1;
    final BlockingQueue<BigInteger> answer = new LinkedBlockingQueue<>();

    public BigInteger getResult() {
        boolean interrupted = false;
        try {
            for (;;) {
                try {
                    return answer.take();
                } catch (InterruptedException ignore) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        sendNumbers();
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, final BigInteger msg) {
        receivedMessages ++;
        if (receivedMessages == CustomProtocolClient.COUNT) {
            // 计算完毕，将结果放入answer中
            ctx.channel().close().addListener(future -> {
                boolean offered = answer.offer(msg);
                assert offered;
            });
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }

    private void sendNumbers() {
        // 最大计算2的1000次方
        ChannelFuture future = null;
        for (int i = 0; i < 1000 && next <= CustomProtocolClient.COUNT; i++) {
            future = ctx.write(2);
            next++;
        }
        if (next <= CustomProtocolClient.COUNT) {
            assert future != null;
            future.addListener(numberSender);
        }
        ctx.flush();
    }

    private final ChannelFutureListener numberSender = future -> {
        if (future.isSuccess()) {
            sendNumbers();
        } else {
            future.cause().printStackTrace();
            future.channel().close();
        }
    };
}
