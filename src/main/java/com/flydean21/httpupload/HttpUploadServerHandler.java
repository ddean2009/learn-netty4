
package com.flydean21.httpupload;

import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.cookie.ServerCookieEncoder;
import io.netty.handler.codec.http.multipart.*;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.EndOfDataDecoderException;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder.ErrorDataDecoderException;
import io.netty.handler.codec.http.multipart.InterfaceHttpData.HttpDataType;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static io.netty.buffer.Unpooled.copiedBuffer;

@Slf4j
public class HttpUploadServerHandler extends SimpleChannelInboundHandler<HttpObject> {

    private HttpRequest request;

    private HttpData partialContent;

    private final StringBuilder responseContent = new StringBuilder();

    private static final HttpDataFactory factory =
            new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private HttpPostRequestDecoder decoder;

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = true; // 退出的时候删除临时文件
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true; // 退出的时候删除临时文件
        DiskAttribute.baseDirectory = null;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (decoder != null) {
            decoder.cleanFiles();
        }
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, HttpObject msg) throws Exception {
        //处理HttpRequest
        if (msg instanceof HttpRequest) {
            HttpRequest request = this.request = (HttpRequest) msg;

            responseContent.setLength(0);
            responseContent.append("请求响应开始\r\n");
            responseContent.append("===================================\r\n");
            responseContent.append("VERSION: ").append(request.protocolVersion().text()).append("\r\n");
            responseContent.append("REQUEST_URI: ").append(request.uri()).append("\r\n");

            // 添加header值
            for (Entry<String, String> entry : request.headers()) {
                responseContent.append("HEADER: ").append(entry.getKey()).append('=').append(entry.getValue()).append("\r\n");
            }

            // 设置cookie值
            Set<Cookie> cookies;
            String value = request.headers().get(HttpHeaderNames.COOKIE);
            if (value == null) {
                cookies = Collections.emptySet();
            } else {
                cookies = ServerCookieDecoder.STRICT.decode(value);
            }
            for (Cookie cookie : cookies) {
                responseContent.append("COOKIE: ").append(cookie).append("\r\n");
            }

            //解析URL中的参数
            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.uri());
            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
            for (Entry<String, List<String>> attr: uriAttributes.entrySet()) {
                for (String attrVal: attr.getValue()) {
                    responseContent.append("URI: ").append(attr.getKey()).append('=').append(attrVal).append("\r\n");
                }
            }

            //GET请求
            if (HttpMethod.GET.equals(request.method())) {
                responseContent.append("\r\n\r\nEND OF GET CONTENT\r\n");
                return;
            }
            try {
                //POST请求
                decoder = new HttpPostRequestDecoder(factory, request);
            } catch (ErrorDataDecoderException e1) {
                // 异常处理
                log.error("出现异常",e1);
                responseContent.append(e1.getMessage());
                writeResponse(ctx.channel(), true);
                return;
            }

            boolean readingChunks = HttpUtil.isTransferEncodingChunked(request);
            responseContent.append("Is Chunked: ").append(readingChunks).append("\r\n");
            responseContent.append("IsMultipart: ").append(decoder.isMultipart()).append("\r\n");
            if (readingChunks) {
                responseContent.append("Chunks: ");
            }
        }

        //处理HttpContent
        // 如果POST的decoder存在
        if (decoder != null) {
            if (msg instanceof HttpContent) {
                HttpContent chunk = (HttpContent) msg;
                try {
                    decoder.offer(chunk);
                } catch (ErrorDataDecoderException e1) {
                    // 异常处理
                    log.error("出现异常",e1);
                    responseContent.append(e1.getMessage());
                    writeResponse(ctx.channel(), true);
                    return;
                }
                //读取httpData
                log.info("readHttpDataChunkByChunk");
                readHttpDataChunkByChunk();
                // 读最后一部分
                if (chunk instanceof LastHttpContent) {
                    writeResponse(ctx.channel());
                    reset();
                }
            }
        } else {
            writeResponse(ctx.channel());
        }
    }

    private void reset() {
        request = null;
        //destory decoder
        decoder.destroy();
        decoder = null;
    }

    /**
     * 读取HttpData
     */
    private void readHttpDataChunkByChunk() {
        try {
            while (decoder.hasNext()) {
                log.info("decoder has next");
                InterfaceHttpData data = decoder.next();
                if (data != null) {
                    // 检测当前的 HttpData 如果是一个 FileUpload 并且和上一个 partialContent一致，表示是最后一个HttpData
                    if (partialContent == data) {
                        log.info(" 100% (FinalSize: " + partialContent.length() + ")");
                        partialContent = null;
                    }
                    // new value
                    writeHttpData(data);
                }
            }
            //如果decoder并没有结束
            // 检测当前的 partial 数据
            InterfaceHttpData data = decoder.currentPartialHttpData();
            if (data != null) {
                StringBuilder builder = new StringBuilder();
                if (partialContent == null) {
                    partialContent = (HttpData) data;
                    if (partialContent instanceof FileUpload) {
                        builder.append("Start FileUpload: ")
                            .append(((FileUpload) partialContent).getFilename()).append(" ");
                    } else {
                        builder.append("Start Attribute: ")
                            .append(partialContent.getName()).append(" ");
                    }
                    builder.append("(DefinedSize: ").append(partialContent.definedLength()).append(")");
                }
                if (partialContent.definedLength() > 0) {
                    builder.append(" ").append(partialContent.length() * 100 / partialContent.definedLength())
                        .append("% ");
                    log.info(builder.toString());
                } else {
                    builder.append(" ").append(partialContent.length()).append(" ");
                    log.info(builder.toString());
                }
            }
        } catch (EndOfDataDecoderException e1) {
            // end
            responseContent.append("END OF CONTENT CHUNK BY CHUNK\r\n\r\n");
        }
    }

    private void writeHttpData(InterfaceHttpData data) {
        if (data.getHttpDataType() == HttpDataType.Attribute) {
            Attribute attribute = (Attribute) data;
            String value;
            try {
                value = attribute.getValue();
            } catch (IOException e1) {
                // 异常处理
                log.error("出现异常",e1);
                responseContent.append("BODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute.getName()).append(" 读取数据出错: ").append(e1.getMessage()).append("\r\n");
                return;
            }
            if (value.length() > 100) {
                responseContent.append("BODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute.getName()).append("数据太长了\r\n");
            } else {
                responseContent.append("BODY Attribute: ").append(attribute.getHttpDataType().name()).append(": ").append(attribute).append("\r\n");
            }
        } else {
            responseContent.append("BODY FileUpload: ").append(data.getHttpDataType().name()).append(": ").append(data).append("\r\n");
            if (data.getHttpDataType() == HttpDataType.FileUpload) {
                FileUpload fileUpload = (FileUpload) data;
                if (fileUpload.isCompleted()) {
                    if (fileUpload.length() < 10000) {
                        responseContent.append("文件内容如下:\r\n");
                        try {
                            responseContent.append(fileUpload.getString(fileUpload.getCharset()));
                        } catch (IOException e1) {
                            // 异常处理
                            log.error("出现异常",e1);
                        }
                        responseContent.append("\r\n");
                    } else {
                        responseContent.append("文件太长了:").append(fileUpload.length()).append("\r\n");
                    }
                } else {
                    responseContent.append("文件接收有误!\r\n");
                }
            }
        }
    }

    private void writeResponse(Channel channel) {
        writeResponse(channel, false);
    }

    private void writeResponse(Channel channel, boolean forceClose) {
        ByteBuf buf = copiedBuffer(responseContent.toString(), CharsetUtil.UTF_8);
        responseContent.setLength(0);

        boolean keepAlive = HttpUtil.isKeepAlive(request) && !forceClose;

        FullHttpResponse response = new DefaultFullHttpResponse(
                HttpVersion.HTTP_1_1, HttpResponseStatus.OK, buf);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        response.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());

        if (!keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HttpVersion.HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        Set<Cookie> cookies;
        String value = request.headers().get(HttpHeaderNames.COOKIE);
        if (value == null) {
            cookies = Collections.emptySet();
        } else {
            cookies = ServerCookieDecoder.STRICT.decode(value);
        }
        if (!cookies.isEmpty()) {
            //设置cookie
            for (Cookie cookie : cookies) {
                response.headers().add(HttpHeaderNames.SET_COOKIE, ServerCookieEncoder.STRICT.encode(cookie));
            }
        }
        // 写入response
        ChannelFuture future = channel.writeAndFlush(response);
        // 关闭连接
        if (!keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常处理
        log.error("出现异常",cause);
        ctx.channel().close();
    }
}
