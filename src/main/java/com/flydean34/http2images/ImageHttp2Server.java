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

package com.flydean34.http2images;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.*;
import io.netty.handler.ssl.ApplicationProtocolConfig.Protocol;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectedListenerFailureBehavior;
import io.netty.handler.ssl.ApplicationProtocolConfig.SelectorFailureBehavior;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import static io.netty.handler.codec.http2.Http2SecurityUtil.CIPHERS;

/**
 * 支持http2的server
 */
public class ImageHttp2Server {

    public static final int PORT = 8443;

    private final EventLoopGroup group;

    public ImageHttp2Server(EventLoopGroup eventLoopGroup) {
        group = eventLoopGroup;
    }

    public void start() throws Exception {
        final SslContext sslCtx;
        SelfSignedCertificate ssc = new SelfSignedCertificate();
        ApplicationProtocolConfig apn = new ApplicationProtocolConfig(
                Protocol.ALPN,
                // 目前 OpenSsl 和 JDK providers只支持NO_ADVERTISE
                SelectorFailureBehavior.NO_ADVERTISE,
                // 目前 OpenSsl 和 JDK providers只支持ACCEPT
                SelectedListenerFailureBehavior.ACCEPT,
                ApplicationProtocolNames.HTTP_2,
                ApplicationProtocolNames.HTTP_1_1);

        sslCtx= SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey(), null)
                .ciphers(CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(apn).build();

        ServerBootstrap b = new ServerBootstrap();
        b.option(ChannelOption.SO_BACKLOG, 1024);
        b.group(group).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel ch)  {
                ch.pipeline().addLast(sslCtx.newHandler(ch.alloc()), new CustProtocolNegotiationHandler());
            }
        });

        Channel ch = b.bind(PORT).sync().channel();
        ch.closeFuture();
    }

}
