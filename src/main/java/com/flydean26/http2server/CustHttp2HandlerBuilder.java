

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
