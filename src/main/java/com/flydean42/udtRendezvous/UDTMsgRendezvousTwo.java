
package com.flydean42.udtRendezvous;

import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;

public class UDTMsgRendezvousTwo extends UDTMsgRendezvousBase {

    public UDTMsgRendezvousTwo(final InetSocketAddress self, final InetSocketAddress peer, final int messageSize) {
        super(self, peer, messageSize);
    }

    public static void main(final String[] args) throws Exception {
        final int messageSize = 64 * 1024;
        final InetSocketAddress self = SocketUtils.socketAddress("127.0.0.1", 8001);
        final InetSocketAddress peer = SocketUtils.socketAddress("127.0.0.1", 8000);
        new UDTMsgRendezvousTwo(self, peer, messageSize).run();
    }
}
