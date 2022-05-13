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
