
package com.flydean13.customprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.math.BigInteger;
import java.util.List;

/**
 * 将编码过后的byte[] 转换成BigInteger对象
 */
public class NumberDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        // 保证魔法词和数组长度有效
        if (in.readableBytes() < 5) {
            return;
        }
        in.markReaderIndex();
        // 检查魔法词
        int magicNumber = in.readUnsignedByte();
        if (magicNumber != 'N') {
            in.resetReaderIndex();
            throw new CorruptedFrameException("无效的魔法词: " + magicNumber);
        }
        // 读取所有的数据
        int dataLength = in.readInt();
        if (in.readableBytes() < dataLength) {
            in.resetReaderIndex();
            return;
        }
        // 将剩下的数据转换成为BigInteger
        byte[] decoded = new byte[dataLength];
        in.readBytes(decoded);
        out.add(new BigInteger(decoded));
    }
}
