package com.flydean43.byteBufRef;

import io.netty.buffer.ByteBuf;
import io.netty.util.IllegalReferenceCountException;
import lombok.extern.slf4j.Slf4j;

import static io.netty.buffer.Unpooled.directBuffer;

@Slf4j
public class ByteBufRef {

    public static void main(String[] args) {

        ByteBuf buf = directBuffer();
        assert buf.refCnt() == 1;

        boolean destroyed = buf.release();
        assert destroyed;
        assert buf.refCnt() == 0;

        try {
            buf.writeByte(10);
        } catch (IllegalReferenceCountException e) {
            log.error(e.getMessage(),e);
        }


//        buf.retain();
//        assert buf.refCnt() == 1;

        buf = directBuffer();
        buf.retain();
        assert buf.refCnt() == 2;

        buf = directBuffer();
        ByteBuf derived = buf.duplicate();
        assert buf.refCnt() == 1;
        assert derived.refCnt() == 1;

    }
}
