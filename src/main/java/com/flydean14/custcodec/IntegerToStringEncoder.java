package com.flydean14.custcodec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

/**
 * @author wayne
 * @version IntegerToStringEncoder,  2021/8/13
 */
public class IntegerToStringEncoder extends
        MessageToMessageEncoder<Integer> {

    @Override
    public void encode(ChannelHandlerContext ctx, Integer message, List<Object> out)
            throws Exception {
        out.add(message.toString());
    }
}
