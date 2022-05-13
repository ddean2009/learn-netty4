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
