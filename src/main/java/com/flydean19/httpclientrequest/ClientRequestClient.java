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
package com.flydean19.httpclientrequest;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.net.URI;

/**
 * 一个自定义的HTTP Client
 */
public final class ClientRequestClient {

    static final String URL = System.getProperty("url", "http://127.0.0.1:8000/");

    public static void main(String[] args) throws Exception {
        URI uri = new URI(URL);
        String host = uri.getHost() == null? "127.0.0.1" : uri.getHost();
        int port = uri.getPort();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
             .channel(NioSocketChannel.class)
             .handler(new ClientRequestClientInitializer());

            // 建立连接
            Channel ch = b.connect(host, port).sync().channel();

            // HTTP请求
            HttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.GET, uri.getRawPath(), Unpooled.EMPTY_BUFFER);
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);

            // 设置cookie
            request.headers().set(
                    HttpHeaderNames.COOKIE,
                    ClientCookieEncoder.STRICT.encode(
                            new DefaultCookie("name", "flydean"),
                            new DefaultCookie("site", "www.flydean.com")));

            // 发送HTTP请求
            ch.writeAndFlush(request);
            // 关闭连接
            ch.closeFuture().sync();
        } finally {
            // 关闭服务器
            group.shutdownGracefully();
        }
    }
}
