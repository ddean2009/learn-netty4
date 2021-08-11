
package com.flydean13.customprotocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;

/**
 * 服务器端pipeline初始化
 */
public class CustomProtocolServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // 对流进行压缩
        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));

        // 添加number编码解码器
        pipeline.addLast(new NumberDecoder());
        pipeline.addLast(new NumberEncoder());

        // 添加业务处理逻辑
        pipeline.addLast(new CustomProtocolServerHandler());
    }
}
