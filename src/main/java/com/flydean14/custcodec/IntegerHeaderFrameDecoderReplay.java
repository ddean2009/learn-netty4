package com.flydean14.custcodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author wayne
 * @version IntegerHeaderFrameDecoderReplay,  2021/8/13
 */
public class IntegerHeaderFrameDecoderReplay extends ReplayingDecoder<Void> {

    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf buf, List<Object> out) throws Exception {

        out.add(buf.readBytes(buf.readInt()));
    }
}
