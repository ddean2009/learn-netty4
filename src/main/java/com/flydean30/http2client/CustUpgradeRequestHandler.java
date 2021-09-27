package com.flydean30.http2client;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;

import java.net.InetSocketAddress;

/**
 * upgrade处理器
 */
final class CustUpgradeRequestHandler extends ChannelInboundHandlerAdapter {

    private final CustHttp2ClientInitializer custHttp2ClientInitializer;

    public CustUpgradeRequestHandler(CustHttp2ClientInitializer custHttp2ClientInitializer) {
        this.custHttp2ClientInitializer = custHttp2ClientInitializer;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        DefaultFullHttpRequest upgradeRequest =
                new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/", Unpooled.EMPTY_BUFFER);

        // 设置upgradeRequest的host地址
        InetSocketAddress remote = (InetSocketAddress) ctx.channel().remoteAddress();
        String hostString = remote.getHostString();
        if (hostString == null) {
            hostString = remote.getAddress().getHostAddress();
        }
        upgradeRequest.headers().set(HttpHeaderNames.HOST, hostString + ':' + remote.getPort());

        ctx.writeAndFlush(upgradeRequest);
        ctx.fireChannelActive();
        // 升级完毕，从pipeline中删除
        ctx.pipeline().remove(this);
        ctx.pipeline().addLast(custHttp2ClientInitializer.settingsHandler(), custHttp2ClientInitializer.responseHandler());
    }
}
