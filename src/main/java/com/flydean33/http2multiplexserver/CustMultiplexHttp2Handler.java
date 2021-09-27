

package com.flydean33.http2multiplexserver;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * 多路复用的http2 handler
 */
@Sharable
@Slf4j
public class CustMultiplexHttp2Handler extends ChannelDuplexHandler {

    static final ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("我在使用http2呀", CharsetUtil.UTF_8));

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        // 异常处理
        log.error("出现异常",cause);
        ctx.close();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Http2HeadersFrame) {
            onHeadersRead(ctx, (Http2HeadersFrame) msg);
        } else if (msg instanceof Http2DataFrame) {
            onDataRead(ctx, (Http2DataFrame) msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    /**
     * 处理Http2DataFrame数据
     */
    private static void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data) {
        if (data.isEndStream()) {
            sendResponse(ctx, data.content());
        } else {
            // 释放data
            data.release();
        }
    }

    /**
     * 处理Http2HeadersFrame消息
     */
    private static void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers){
        if (headers.isEndStream()) {
            ByteBuf content = ctx.alloc().buffer();
            content.writeBytes(RESPONSE_BYTES.duplicate());
            ByteBufUtil.writeUtf8(content, " - 使用 HTTP/2");
            sendResponse(ctx, content);
        }
    }

    /**
     * 发送响应
     */
    private static void sendResponse(ChannelHandlerContext ctx, ByteBuf payload) {
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        //支持中文
        headers.set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=utf-8");
        ctx.write(new DefaultHttp2HeadersFrame(headers));
        ctx.write(new DefaultHttp2DataFrame(payload, true));
    }
}
