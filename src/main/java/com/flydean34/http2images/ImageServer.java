
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
