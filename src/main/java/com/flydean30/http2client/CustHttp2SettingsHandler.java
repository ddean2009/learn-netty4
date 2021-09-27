
package com.flydean30.http2client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http2.Http2Settings;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 处理 Http2Setting
 */
@Slf4j
public class CustHttp2SettingsHandler extends SimpleChannelInboundHandler<Http2Settings> {
    private final ChannelPromise promise;

    public CustHttp2SettingsHandler(ChannelPromise promise) {
        this.promise = promise;
    }

    /**
     * 等待设置完毕
     */
    public void awaitSettings(long timeout, TimeUnit unit){
        if (!promise.awaitUninterruptibly(timeout, unit)) {
            throw new IllegalStateException("设置时间超时");
        }
        if (!promise.isSuccess()) {
            throw new RuntimeException(promise.cause());
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Http2Settings msg) throws Exception {
        log.info("接收到Http2Settings消息:{}",msg);
        promise.setSuccess();
        //处理完毕，删除handler
        ctx.pipeline().remove(this);
    }
}
