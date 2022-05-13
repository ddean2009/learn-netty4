
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
package com.flydean26.http2server;

import io.netty.handler.codec.http2.*;

import static io.netty.handler.logging.LogLevel.INFO;

public final class CustHttp2HandlerBuilder
        extends AbstractHttp2ConnectionHandlerBuilder<CustHttp2Handler, CustHttp2HandlerBuilder> {

    private static final Http2FrameLogger logger = new Http2FrameLogger(INFO, CustHttp2Handler.class);

    public CustHttp2HandlerBuilder() {
        frameLogger(logger);
    }

    @Override
    public CustHttp2Handler build() {
        return super.build();
    }

    @Override
    protected CustHttp2Handler build(Http2ConnectionDecoder decoder, Http2ConnectionEncoder encoder,
                                 Http2Settings initialSettings) {
        CustHttp2Handler handler = new CustHttp2Handler(decoder, encoder, initialSettings);
        frameListener(handler);
        return handler;
    }
}
