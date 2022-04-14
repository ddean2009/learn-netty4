
package com.flydean58.proxyprotocol;

import io.netty.channel.*;
import io.netty.handler.codec.haproxy.HAProxyMessage;
import io.netty.handler.codec.haproxy.HAProxyMessageEncoder;

public class ClientHander extends ChannelOutboundHandlerAdapter {

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        ctx.pipeline().addBefore(ctx.name(), null, HAProxyMessageEncoder.INSTANCE);
        super.handlerAdded(ctx);
    }

    @Override
    public void write(final ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        ChannelFuture future1 = ctx.write(msg, promise);
        if (msg instanceof HAProxyMessage) {
            future1.addListener((ChannelFutureListener) future2 -> {
                if (future2.isSuccess()) {
                    ctx.pipeline().remove(HAProxyMessageEncoder.INSTANCE);
                    ctx.pipeline().remove(ClientHander.this);
                } else {
                    ctx.close();
                }
            });
        }
    }
}
