
package com.flydean61.memcached;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.memcache.binary.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MemcachedClientHandler extends ChannelDuplexHandler {

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
        String command = (String) msg;
        if (command.startsWith("get ")) {
            String keyString = command.substring("get ".length());
            ByteBuf key = Unpooled.wrappedBuffer(keyString.getBytes(CharsetUtil.UTF_8));

            BinaryMemcacheRequest req = new DefaultBinaryMemcacheRequest(key);
            req.setOpcode(BinaryMemcacheOpcodes.GET);

            ctx.write(req, promise);
        } else if (command.startsWith("set ")) {
            String[] parts = command.split(" ", 3);
            if (parts.length < 3) {
                throw new IllegalArgumentException("命令格式异常: " + command);
            }
            String keyString = parts[1];
            String value = parts[2];

            ByteBuf key = Unpooled.wrappedBuffer(keyString.getBytes(CharsetUtil.UTF_8));
            ByteBuf content = Unpooled.wrappedBuffer(value.getBytes(CharsetUtil.UTF_8));
            ByteBuf extras = ctx.alloc().buffer(8);
            extras.writeZero(8);

            BinaryMemcacheRequest req = new DefaultFullBinaryMemcacheRequest(key, extras, content);
            req.setOpcode(BinaryMemcacheOpcodes.SET);

            ctx.write(req, promise);
        } else {
            throw new IllegalStateException("未知消息: " + msg);
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullBinaryMemcacheResponse res = (FullBinaryMemcacheResponse) msg;
        log.info("channelRead: {}",res.content().toString(CharsetUtil.UTF_8));
        res.release();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("caught exception: {}", cause.getMessage());
        ctx.close();
    }
}
