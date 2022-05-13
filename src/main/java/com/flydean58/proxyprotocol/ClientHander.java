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
