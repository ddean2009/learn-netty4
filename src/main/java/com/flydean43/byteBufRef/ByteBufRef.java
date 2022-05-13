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
