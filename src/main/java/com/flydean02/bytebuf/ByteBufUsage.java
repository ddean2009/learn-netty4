package com.flydean02.bytebuf;

import io.netty.buffer.ByteBuf;

import java.util.Random;

import static io.netty.buffer.Unpooled.*;

/**
 * @author wayne
 * @version ByteBufUsage,  2021/8/2
 */
public class ByteBufUsage {

    public static void main(String[] args) {
        //创建ByteBuf
        ByteBuf heapBuffer    = buffer(128);
        ByteBuf directBuffer  = directBuffer(256);
        ByteBuf wrappedBuffer = wrappedBuffer(new byte[128], new byte[256]);

        //随机访问
        ByteBuf buffer = heapBuffer;
        for (int i = 0; i < buffer.capacity(); i ++) {
            byte b = buffer.getByte(i);
            System.out.println((char) b);
        }

        //遍历readable bytes
        while (directBuffer.isReadable()) {
            System.out.println(directBuffer.readByte());
        }

        //写入writable bytes
        while (wrappedBuffer.maxWritableBytes() >= 4) {
            wrappedBuffer.writeInt(new Random().nextInt());
        }



    }
}
