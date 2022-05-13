/*
 * Copyright 2022 learn-netty4 Project
 *
 * The learn-netty4 Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.flydean30.http2client;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 一个使用HTTP1-style InboundHttp2ToHttpAdapter方式发送HTTP2 frames的客户端
 */
@Slf4j
public final class CustHttp2Client {

    static final boolean SSL = true;
    static final String HOST = System.getProperty("host", "127.0.0.1");
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8000"));
    static final String GETURL = "/";
    static final String POSTURL = "/post";
    static final String POSTDATA = "我是POST数据";

    public static void main(String[] args) throws Exception {
        // SSL配置
        final SslContext sslCtx;
        if (SSL) {
            SslProvider provider =
                    SslProvider.isAlpnSupported(SslProvider.OPENSSL)? SslProvider.OPENSSL : SslProvider.JDK;
            sslCtx = SslContextBuilder.forClient()
                    .sslProvider(provider)
                    .ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                    // 因为我们的证书是自生成的，所以需要信任放行
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .applicationProtocolConfig(new ApplicationProtocolConfig(
                            Protocol.ALPN,
                            SelectorFailureBehavior.NO_ADVERTISE,
                            SelectedListenerFailureBehavior.ACCEPT,
                            ApplicationProtocolNames.HTTP_2,
                            ApplicationProtocolNames.HTTP_1_1))
                    .build();
        } else {
            sslCtx = null;
        }

        EventLoopGroup workerGroup = new NioEventLoopGroup();
        CustHttp2ClientInitializer initializer = new CustHttp2ClientInitializer(sslCtx, Integer.MAX_VALUE);

        try {
            // 配置client
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.remoteAddress(HOST, PORT);
            b.handler(initializer);

            Channel channel = b.connect().syncUninterruptibly().channel();
            log.info("连接到 [" + HOST + ':' + PORT + ']');

            //等待 HTTP/2 升级
            CustHttp2SettingsHandler http2SettingsHandler = initializer.settingsHandler();
            http2SettingsHandler.awaitSettings(5, TimeUnit.SECONDS);

            CustHttpResponseHandler responseHandler = initializer.responseHandler();
            int streamId = 3;
            HttpScheme scheme = SSL ? HttpScheme.HTTPS : HttpScheme.HTTP;
            AsciiString hostName = new AsciiString(HOST + ':' + PORT);
            log.info("发送请求...");
            if (GETURL != null) {
                // 创建一个get请求
                FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, GET, GETURL, Unpooled.EMPTY_BUFFER);
                request.headers().add(HttpHeaderNames.HOST, hostName);
                request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
                responseHandler.put(streamId, channel.write(request), channel.newPromise());
                streamId += 2;
            }
            if (POSTURL != null) {
                // 创建一个post请求
                FullHttpRequest request = new DefaultFullHttpRequest(HTTP_1_1, POST, POSTURL,
                        wrappedBuffer(POSTDATA.getBytes(CharsetUtil.UTF_8)));
                request.headers().add(HttpHeaderNames.HOST, hostName);
                request.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), scheme.name());
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
                request.headers().add(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.DEFLATE);
                responseHandler.put(streamId, channel.write(request), channel.newPromise());
            }
            channel.flush();
            responseHandler.awaitResponses(5, TimeUnit.SECONDS);
            log.info("HTTP/2 请求完毕");

            channel.close().syncUninterruptibly();
        } finally {
            workerGroup.shutdownGracefully();
        }
    }
}
