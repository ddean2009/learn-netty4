

package com.flydean33.http2multiplexserver;

import com.flydean26.http2server.CustHttp1Handler;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler.UpgradeCodecFactory;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import static io.netty.util.internal.ObjectUtil.checkPositiveOrZero;

/**
 * 配置使用多路复用http2
 */
@Slf4j
public class MultiplexHttp2ServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final UpgradeCodecFactory upgradeCodecFactory = protocol -> {
        if (AsciiString.contentEquals(Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
            return new Http2ServerUpgradeCodec(
                    Http2FrameCodecBuilder.forServer().build(),
                    new Http2MultiplexHandler(new CustMultiplexHttp2Handler()));
        } else {
            return null;
        }
    };

    private final SslContext sslCtx;
    private final int maxHttpContentLength;

    public MultiplexHttp2ServerInitializer(SslContext sslCtx) {
        this(sslCtx, 16 * 1024);
    }

    public MultiplexHttp2ServerInitializer(SslContext sslCtx, int maxHttpContentLength) {
        this.sslCtx = sslCtx;
        this.maxHttpContentLength = checkPositiveOrZero(maxHttpContentLength, "maxHttpContentLength");
    }

    @Override
    public void initChannel(SocketChannel ch) {
        if (sslCtx != null) {
            configureSsl(ch);
        } else {
            configureClearText(ch);
        }
    }

    /**
     * 配置TLS的http2处理器
     */
    private void configureSsl(SocketChannel ch) {
        ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new MultiplexProtocolNegotiationHandler());
    }

    /**
     * 配置 cleartext 从HTTP1.1 升级到 HTTP/2.0
     */
    private void configureClearText(SocketChannel ch) {
        final ChannelPipeline p = ch.pipeline();
        final HttpServerCodec sourceCodec = new HttpServerCodec();

        p.addLast(sourceCodec);
        p.addLast(new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory));
        p.addLast(new SimpleChannelInboundHandler<HttpMessage>() {
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HttpMessage msg) {
                // 如果是HttpMessage，则说明是直接使用HTTP1，没有升级
                log.info("直接使用: {}, 无升级版本" + msg.protocolVersion());
                ChannelPipeline pipeline = ctx.pipeline();
                pipeline.addAfter(ctx.name(), null, new CustHttp1Handler("直接使用，无升级"));
                pipeline.replace(this, null, new HttpObjectAggregator(maxHttpContentLength));
                ctx.fireChannelRead(ReferenceCountUtil.retain(msg));
            }
        });
        p.addLast(new UserEventLogger());
    }

    /**
     * 记录用户触发的事件
     */
    private static class UserEventLogger extends ChannelInboundHandlerAdapter {
        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
            log.info("用户触发事件: " + evt);
            ctx.fireUserEventTriggered(evt);
        }
    }
}
