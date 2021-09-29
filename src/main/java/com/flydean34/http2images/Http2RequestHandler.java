

package com.flydean34.http2images;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http2.HttpConversionUtil;

import java.io.IOException;
import java.util.List;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.util.internal.ObjectUtil.checkNotNull;
import static java.lang.Integer.parseInt;

/**
 * 支持http2的处理器，因为添加了InboundHttp2ToHttpAdapter对http2的消息格式进行了转换，所以这里处理的是FullHttpRequest
 */
public class Http2RequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String IMAGE_ID = "id";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        QueryStringDecoder queryString = new QueryStringDecoder(request.uri());
        String streamId = getStreamId(request);
        String id = getValue(queryString, IMAGE_ID);
        if (id == null) {
            handlePage(ctx, streamId, request);
        } else {
            handleImage(id, ctx, streamId, request);
        }
    }

    private void handleImage(String id, ChannelHandlerContext ctx, String streamId,
            FullHttpRequest request) {
        ByteBuf image = ImagePage.getImage(parseInt(id));
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, image);
        response.headers().set(CONTENT_TYPE, "image/jpeg");
        sendResponse(ctx, streamId, response, request);
    }

    private void handlePage(ChannelHandlerContext ctx, String streamId,  FullHttpRequest request) throws IOException {
        ByteBuf content =ImagePage.getContent();
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, content);
        response.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        sendResponse(ctx, streamId, response, request);
    }

    protected void sendResponse(final ChannelHandlerContext ctx, String streamId,
            final FullHttpResponse response, final FullHttpRequest request) {
        setContentLength(response, response.content().readableBytes());
        setStreamId(response, streamId);
        ctx.writeAndFlush(response);
    }

    private static String getStreamId(FullHttpRequest request) {
        return request.headers().get(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text());
    }

    private static void setStreamId(FullHttpResponse response, String streamId) {
        response.headers().set(HttpConversionUtil.ExtensionHeaderNames.STREAM_ID.text(), streamId);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }


    /**
     * 从query参数中获取值
     */
    public static String getValue(QueryStringDecoder query, String key) {
        checkNotNull(query, "查询参数不能为空!");
        List<String> values = query.parameters().get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.get(0);
    }
}
