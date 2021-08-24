
package com.flydean21.httpupload;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * 上传文件处理器
 */
@Slf4j
public class HttpUploadClientHandler extends SimpleChannelInboundHandler<HttpObject> {

    private boolean readingChunks;

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
        if (msg instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) msg;

            log.info("STATUS: " + response.status());
            log.info("VERSION: " + response.protocolVersion());

            if (!response.headers().isEmpty()) {
                for (CharSequence name : response.headers().names()) {
                    for (CharSequence value : response.headers().getAll(name)) {
                        log.info("HEADER: " + name + " = " + value);
                    }
                }
            }

            if (response.status().code() == 200 && HttpUtil.isTransferEncodingChunked(response)) {
                readingChunks = true;
                log.info("CHUNKED CONTENT {");
            } else {
                log.info("CONTENT {");
            }
        }
        if (msg instanceof HttpContent) {
            HttpContent chunk = (HttpContent) msg;

            if (chunk instanceof LastHttpContent) {
                log.info(chunk.content().toString(CharsetUtil.UTF_8));
                if (readingChunks) {
                    log.info("} END OF CHUNKED CONTENT");
                } else {
                    log.info("} END OF CONTENT");
                }
                readingChunks = false;
            } else {
                log.info(chunk.content().toString(CharsetUtil.UTF_8));
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        // 异常处理
        log.error("出现异常",cause);
        ctx.channel().close();
    }
}
