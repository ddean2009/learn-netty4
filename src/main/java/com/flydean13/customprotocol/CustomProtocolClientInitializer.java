
package com.flydean13.customprotocol;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;

/**
 * 客户端pipeline初始化器
 */
public class CustomProtocolClientInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        // stream压缩
        pipeline.addLast(ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        pipeline.addLast(ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));

        // 添加Number编码器
        pipeline.addLast(new NumberDecoder());
        pipeline.addLast(new NumberEncoder());

        // 最后添加业务逻辑
        pipeline.addLast(new CustomProtocolClientHandler());
    }
}
