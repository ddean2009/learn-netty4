
package com.flydean30.http2framecodec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http2.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

import static io.netty.buffer.Unpooled.copiedBuffer;
import static io.netty.buffer.Unpooled.unreleasableBuffer;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;

/**
 * 和frame codec配合使用的Http2 处理器
 */
@Slf4j
@Sharable
public class CustHttp2Handler extends Http2ChannelDuplexHandler {

    static final ByteBuf RESPONSE_BYTES = unreleasableBuffer(copiedBuffer("我在使用HTTP2", CharsetUtil.UTF_8));

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
     * 处理data frame消息
     */
    private static void onDataRead(ChannelHandlerContext ctx, Http2DataFrame data){
        Http2FrameStream stream = data.stream();
        if (data.isEndStream()) {
            sendResponse(ctx, stream, data.content());
        } else {
            // 不是end stream不发送，但是需要释放引用
            data.release();
        }
        // 处理完data，需要更新window frame，增加处理过的Data大小
        ctx.write(new DefaultHttp2WindowUpdateFrame(data.initialFlowControlledBytes()).stream(stream));
    }

    /**
     * 处理header frame消息
     */
    private static void onHeadersRead(ChannelHandlerContext ctx, Http2HeadersFrame headers) {
        if (headers.isEndStream()) {
            ByteBuf content = ctx.alloc().buffer();
            content.writeBytes(RESPONSE_BYTES.duplicate());
            ByteBufUtil.writeUtf8(content, " - 使用 HTTP/2");
            sendResponse(ctx, headers.stream(), content);
        }
    }

    /**
     * 发送响应到客户端
     */
    private static void sendResponse(ChannelHandlerContext ctx, Http2FrameStream stream, ByteBuf payload) {
        Http2Headers headers = new DefaultHttp2Headers().status(OK.codeAsText());
        //支持中文
        headers.set(HttpHeaderNames.CONTENT_TYPE,"text/plain;charset=utf-8");
        ctx.write(new DefaultHttp2HeadersFrame(headers).stream(stream));
        ctx.write(new DefaultHttp2DataFrame(payload, true).stream(stream));
    }
}
