package com.flydean14.custcodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author wayne
 * @version IntegerHeaderFrameDecoder,  2021/8/13
 */
public class IntegerHeaderFrameDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf buf, List<Object> out) throws Exception {

        if (buf.readableBytes() < 4) {
            return;
        }

        buf.markReaderIndex();
        int length = buf.readInt();

        if (buf.readableBytes() < length) {
            buf.resetReaderIndex();
            return;
        }

        out.add(buf.readBytes(length));
    }
}
