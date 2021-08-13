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