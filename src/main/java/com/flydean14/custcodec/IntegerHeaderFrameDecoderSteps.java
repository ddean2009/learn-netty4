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
package com.flydean14.custcodec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

/**
 * @author wayne
 * @version IntegerHeaderFrameDecoderSteps,  2021/8/13
 */
public class IntegerHeaderFrameDecoderSteps extends ReplayingDecoder<MyDecoderState> {

    private int length;

    public IntegerHeaderFrameDecoderSteps() {
        // Set the initial state.
        super(MyDecoderState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx,
                          ByteBuf buf, List<Object> out) throws Exception {
        switch (state()) {
            case READ_LENGTH:
                length = buf.readInt();
                checkpoint(MyDecoderState.READ_CONTENT);
            case READ_CONTENT:
                ByteBuf frame = buf.readBytes(length);
                checkpoint(MyDecoderState.READ_LENGTH);
                out.add(frame);
                break;
            default:
                throw new Error("Shouldn't reach here.");
        }
    }
}