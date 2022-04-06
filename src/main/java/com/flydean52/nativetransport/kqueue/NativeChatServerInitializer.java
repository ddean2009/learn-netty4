
package com.flydean52.nativetransport.kqueue;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 初始化一个ChannelPipeline
 */
public class NativeChatServerInitializer extends ChannelInitializer<SocketChannel> {

    private static final StringDecoder DECODER = new StringDecoder();
    private static final StringEncoder ENCODER = new StringEncoder();

    private static final NativeChatServerHandler SERVER_HANDLER = new NativeChatServerHandler();

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        // 添加行分割器
        pipeline.addLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
        // 添加String Decoder和String Encoder,用来进行字符串的转换
        pipeline.addLast(DECODER);
        pipeline.addLast(ENCODER);
        // 最后添加真正的处理器
        pipeline.addLast(SERVER_HANDLER);
    }
}
