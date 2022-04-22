package com.flydean61.memcached;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheClientCodec;
import io.netty.handler.codec.memcache.binary.BinaryMemcacheObjectAggregator;

class MemcachedInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) {
        ChannelPipeline p = ch.pipeline();
        p.addLast(new BinaryMemcacheClientCodec());
        p.addLast(new BinaryMemcacheObjectAggregator(Integer.MAX_VALUE));
        p.addLast(new MemcachedClientHandler());
    }
}
