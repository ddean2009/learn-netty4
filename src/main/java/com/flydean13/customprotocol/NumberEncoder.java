
package com.flydean13.customprotocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.math.BigInteger;

/**
 * 将Number编码成byte[]格式，第一个byte表示魔法词"N"，
 * 接下来的4个byte代表数组的长度
 * 最后的才是真正的数字
 */
public class NumberEncoder extends MessageToByteEncoder<Number> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Number msg, ByteBuf out) {
        // 将number编码成为ByteBuf
        BigInteger v;
        if (msg instanceof BigInteger) {
            v = (BigInteger) msg;
        } else {
            v = new BigInteger(String.valueOf(msg));
        }

        // 将BigInteger转换成为byte[]数组
        byte[] data = v.toByteArray();
        int dataLength = data.length;

        // 将Number进行编码
        out.writeByte((byte) 'N'); // 魔法词
        out.writeInt(dataLength);  // 数组长度
        out.writeBytes(data);      // 最终的数据
    }
}
