
package com.flydean32.http2framecodecclient;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2StreamFrame;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 处理 Http2StreamFrame 响应.
 */
@Slf4j
public final class Http2ClientStreamFrameHandler extends SimpleChannelInboundHandler<Http2StreamFrame> {

    private final CountDownLatch latch = new CountDownLatch(1);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2StreamFrame msg) throws Exception {
        log.info("接收 Http2StreamFrame: {}",msg);

        // 判断stream是否endStream
        if (msg instanceof Http2DataFrame && ((Http2DataFrame) msg).isEndStream()) {
            log.info("Http2DataFrame接收到endStream flag");
            log.info("msg:{}",((Http2DataFrame) msg).content().getCharSequence(0,33, CharsetUtil.UTF_8));
            latch.countDown();
        } else if (msg instanceof Http2HeadersFrame && ((Http2HeadersFrame) msg).isEndStream()) {
            log.info("Http2HeadersFrame接收到endStream flag");
            latch.countDown();
        }
    }

    /**
     * 等待latch countDown或者超时5秒钟
     * @return true 表示成功接收到了一个endStream
     */
    public boolean responseSuccessfullyCompleted() {
        try {
            return latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            log.error("Latch exception: {}" + ie.getMessage());
            return false;
        }
    }

}
