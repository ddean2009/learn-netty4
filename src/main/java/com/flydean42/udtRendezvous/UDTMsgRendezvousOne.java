
package com.flydean42.udtRendezvous;

import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;


public class UDTMsgRendezvousOne extends UDTMsgRendezvousBase {

    public UDTMsgRendezvousOne(final InetSocketAddress self, final InetSocketAddress peer, final int messageSize) {
        super(self, peer, messageSize);
    }

    public static void main(final String[] args) throws Exception {
        final int messageSize = 64 * 1024;
        final InetSocketAddress self = SocketUtils.socketAddress("127.0.0.1", 8000);
        final InetSocketAddress peer = SocketUtils.socketAddress("127.0.0.1", 8001);
        new UDTMsgRendezvousOne(self, peer, messageSize).run();
    }
}
