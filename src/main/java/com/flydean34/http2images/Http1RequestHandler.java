

package com.flydean34.http2images;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpUtil;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.CLOSE;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Http1.1处理器
 */
public final class Http1RequestHandler extends Http2RequestHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.write(new DefaultFullHttpResponse(HTTP_1_1, CONTINUE, Unpooled.EMPTY_BUFFER));
        }
        super.channelRead0(ctx, request);
    }

    @Override
    protected void sendResponse(final ChannelHandlerContext ctx, String streamId,
            final FullHttpResponse response, final FullHttpRequest request) {
        HttpUtil.setContentLength(response, response.content().readableBytes());
            if (isKeepAlive(request)) {
                if (request.protocolVersion().equals(HTTP_1_0)) {
                    response.headers().set(CONNECTION, KEEP_ALIVE);
                }
                ctx.writeAndFlush(response);
            } else {
                // 关闭连接
                response.headers().set(CONNECTION, CLOSE);
                ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
            }
    }
}
