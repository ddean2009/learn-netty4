
package com.flydean42.udtRendezvous;

import io.netty.util.internal.SocketUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


public class UDTByteRendezvousTwo extends UDTByteRendezvousBase {

    public UDTByteRendezvousTwo(int messageSize, SocketAddress myAddress, SocketAddress peerAddress) {
        super(messageSize, myAddress, peerAddress);
    }

    public static void main(String[] args) throws Exception {
        final int messageSize = 64 * 1024;
        final InetSocketAddress myAddress = SocketUtils.socketAddress("127.0.0.1", 8001);
        final InetSocketAddress peerAddress = SocketUtils.socketAddress("127.0.0.1", 8000);
        new UDTByteRendezvousTwo(messageSize, myAddress, peerAddress).run();
    }
}
