package com.flydean14.custcodec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @author wayne
 * @version StringToIntegerDecoder,  2021/8/13
 */
public class StringToIntegerDecoder extends
        MessageToMessageDecoder<String> {

    @Override
    public void decode(ChannelHandlerContext ctx, String message,
                       List<Object> out) throws Exception {
        out.add(message.length());
    }
}