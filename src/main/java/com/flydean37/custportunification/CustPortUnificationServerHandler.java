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
package com.flydean37.custportunification;

import com.flydean19.httpclientrequest.HttpRequestServerHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.compression.ZlibWrapper;
import io.netty.handler.codec.http.HttpContentCompressor;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

import java.util.List;

/**
 * 同时支持gzip和http的handler
 */
public class CustPortUnificationServerHandler extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        final int readerIndex = in.readerIndex();
        if (in.writerIndex() == readerIndex) {
            return;
        }
            final int magic1 = in.getUnsignedByte(readerIndex);
            final int magic2 = in.getUnsignedByte(in.readerIndex() + 1);
            if (isGzip(magic1,magic2)) {
                enableGzip(ctx);
            } else if (isHttp(magic1,magic2)) {
                switchToHttp(ctx);
            } else {
                // 未知协议，关闭连接
                in.clear();
                ctx.close();
        }
    }

    private boolean isGzip(int magic1, int magic2) {
        return magic1 == 31 && magic2 == 139;
    }

    private static boolean isHttp(int magic1, int magic2) {
        return
                magic1 == 'G' && magic2 == 'E' || // GET
                        magic1 == 'P' && magic2 == 'O' || // POST
                        magic1 == 'P' && magic2 == 'U' || // PUT
                        magic1 == 'H' && magic2 == 'E' || // HEAD
                        magic1 == 'O' && magic2 == 'P' || // OPTIONS
                        magic1 == 'P' && magic2 == 'A' || // PATCH
                        magic1 == 'D' && magic2 == 'E' || // DELETE
                        magic1 == 'T' && magic2 == 'R' || // TRACE
                        magic1 == 'C' && magic2 == 'O';   // CONNECT
    }

    private void enableGzip(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("gzipEncoder", ZlibCodecFactory.newZlibEncoder(ZlibWrapper.GZIP));
        p.addLast("gzipDecoder", ZlibCodecFactory.newZlibDecoder(ZlibWrapper.GZIP));
        p.remove(this);
    }

    private void switchToHttp(ChannelHandlerContext ctx) {
        ChannelPipeline p = ctx.pipeline();
        p.addLast("decoder", new HttpRequestDecoder());
        p.addLast("encoder", new HttpResponseEncoder());
        p.addLast("compressor", new HttpContentCompressor());
        p.addLast("handler", new HttpRequestServerHandler());
        p.remove(this);
    }

}
