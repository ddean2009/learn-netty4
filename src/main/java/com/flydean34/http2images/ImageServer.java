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

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;

/**
 * 一个同时支持HTTP和HTTP2的image服务器
 */
@Slf4j
public final class ImageServer {

    public static void main(String[] args) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        ImageHttp2Server http2 = new ImageHttp2Server(group);
        ImageHttp1Server http = new ImageHttp1Server(group);
        try {
            http2.start();
            log.info("使用你的浏览器访问: " + "http://127.0.0.1:" + ImageHttp1Server.PORT);
            http.start().sync();
        } finally {
            group.shutdownGracefully();
        }
    }
}
