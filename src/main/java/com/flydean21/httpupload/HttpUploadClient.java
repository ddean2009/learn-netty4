
package com.flydean21.httpupload;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.handler.codec.http.multipart.*;
import io.netty.util.internal.SocketUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;

/**
 * 文件上传客户端
 */
@Slf4j
public final class HttpUploadClient {

    static final String BASE_URL = System.getProperty("baseUrl", "http://127.0.0.1:8000/");
    static final String FILE = System.getProperty("file", "file.txt");

    public static void main(String[] args) throws Exception {
        String postSimple = BASE_URL + "post";
        String postFile= BASE_URL + "postmultipart";
        String get= BASE_URL + "get";

        URI uriSimple = new URI(postSimple);
        String host = uriSimple.getHost();
        int port = uriSimple.getPort();

        URI uriFile = new URI(postFile);
        File file = new File(FILE);
        log.info(file.getCanonicalPath());
        if (!file.canRead()) {
            throw new FileNotFoundException(FILE);
        }

        EventLoopGroup group = new NioEventLoopGroup();

        // 创建HttpDataFactory 默认是放在内存空间，当超出MINSIZE则会存放在Disk中
        HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

        DiskFileUpload.deleteOnExitTemporaryFile = true; // 在退出的时候删除文件
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = true;  // 在退出的时候删除文件
        DiskAttribute.baseDirectory = null;

        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
                    .handler(new HttpUploadClientInitializer());

            // Simple Get form
            List<Entry<String, String>> headers = formget(b, host, port, get, uriSimple);
            if (headers == null) {
                factory.cleanAllHttpData();
                return;
            }

            // Simple Post form
            List<InterfaceHttpData> bodylist = formpost(b, host, port, uriSimple, file, factory, headers);
            if (bodylist == null) {
                factory.cleanAllHttpData();
                return;
            }

            // Multipart Post form
            formpostmultipart(b, host, port, uriFile, factory, headers, bodylist);
        } finally {
            group.shutdownGracefully();
            // 清除所有的temp文件
            factory.cleanAllHttpData();
        }
    }

    /**
     * 一个标准的HTTP get请求
     **/
    private static List<Entry<String, String>> formget(
            Bootstrap bootstrap, String host, int port, String get, URI uriSimple) throws Exception {
        Channel channel = bootstrap.connect(host, port).sync().channel();
        // HTTP请求
        QueryStringEncoder encoder = new QueryStringEncoder(get);
        // 添加请求参数
        encoder.addParam("method", "GET");
        encoder.addParam("name", "flydean");
        encoder.addParam("site", "www.flydean.com");

        URI uriGet = new URI(encoder.toString());
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, uriGet.toASCIIString());
        HttpHeaders headers = request.headers();
        headers.set(HttpHeaderNames.HOST, host);
        headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        headers.set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP + "," + HttpHeaderValues.DEFLATE);
        headers.set(HttpHeaderNames.ACCEPT_LANGUAGE, "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
        headers.set(HttpHeaderNames.REFERER, uriSimple.toString());
        headers.set(HttpHeaderNames.USER_AGENT, "Netty Simple Http Client side");
        headers.set(HttpHeaderNames.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");

        headers.set(
                HttpHeaderNames.COOKIE, ClientCookieEncoder.STRICT.encode(
                        new DefaultCookie("name", "flydean"),
                        new DefaultCookie("site", "www.flydean.com"))
        );

        channel.writeAndFlush(request);

        channel.closeFuture().sync();
        // 返回header
        return headers.entries();
    }

    /**
     * 标准POST请求
     */
    private static List<InterfaceHttpData> formpost(
            Bootstrap bootstrap,
            String host, int port, URI uriSimple, File file, HttpDataFactory factory,
            List<Entry<String, String>> headers) throws Exception {

        ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(host, port));
        Channel channel = future.sync().channel();

        // 构建HTTP request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriSimple.toASCIIString());

        // Use the PostBody encoder
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, false);  // false => not multipart

        // 添加headers
        for (Entry<String, String> entry : headers) {
            request.headers().set(entry.getKey(), entry.getValue());
        }

        // 添加form属性
        bodyRequestEncoder.addBodyAttribute("method", "POST");
        bodyRequestEncoder.addBodyAttribute("name", "flydean");
        bodyRequestEncoder.addBodyAttribute("site", "www.flydean.com");
        bodyRequestEncoder.addBodyFileUpload("myfile", file, "application/x-zip-compressed", false);

        // finalize request， 判断是否需要chunk
        request = bodyRequestEncoder.finalizeRequest();

        // 创建bodylist
        List<InterfaceHttpData> bodylist = bodyRequestEncoder.getBodyListAttributes();

        // 发送请求
        channel.write(request);

        // 判断bodyRequestEncoder是否是Chunked，发送请求内容
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();

        //清除请求
        // bodyRequestEncoder.cleanFiles();

        // 等待channel关闭
        channel.closeFuture().sync();
        return bodylist;
    }

    /**
     * Multipart form
     */
    private static void formpostmultipart(
            Bootstrap bootstrap, String host, int port, URI uriFile, HttpDataFactory factory,
            Iterable<Entry<String, String>> headers, List<InterfaceHttpData> bodylist) throws Exception {
        ChannelFuture future = bootstrap.connect(SocketUtils.socketAddress(host, port));
        Channel channel = future.sync().channel();

        // 创建HTTP request
        HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, uriFile.toASCIIString());

        // Use the PostBody encoder
        HttpPostRequestEncoder bodyRequestEncoder =
                new HttpPostRequestEncoder(factory, request, true); // true => multipart

        // 添加headers
        for (Entry<String, String> entry : headers) {
            request.headers().set(entry.getKey(), entry.getValue());
        }
        // 添加body http data
        bodyRequestEncoder.setBodyHttpDatas(bodylist);
        // finalize request，判断是否需要chunk
        request = bodyRequestEncoder.finalizeRequest();
        // 发送请求头
        channel.write(request);
        // 判断bodyRequestEncoder是否是Chunked，发送请求内容
        if (bodyRequestEncoder.isChunked()) {
            channel.write(bodyRequestEncoder);
        }
        channel.flush();
        // 清除文件
        bodyRequestEncoder.cleanFiles();
        channel.closeFuture().sync();
    }



}
