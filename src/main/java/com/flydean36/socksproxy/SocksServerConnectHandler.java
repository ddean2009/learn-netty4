
package com.flydean36.socksproxy;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socksx.SocksMessage;
import io.netty.handler.codec.socksx.v4.DefaultSocks4CommandResponse;
import io.netty.handler.codec.socksx.v4.Socks4CommandRequest;
import io.netty.handler.codec.socksx.v4.Socks4CommandStatus;
import io.netty.handler.codec.socksx.v5.DefaultSocks5CommandResponse;
import io.netty.handler.codec.socksx.v5.Socks5CommandRequest;
import io.netty.handler.codec.socksx.v5.Socks5CommandStatus;
import io.netty.util.concurrent.FutureListener;
import io.netty.util.concurrent.Promise;

public final class SocksServerConnectHandler extends SimpleChannelInboundHandler<SocksMessage> {

    private final Bootstrap b = new Bootstrap();

    @Override
    public void channelRead0(final ChannelHandlerContext ctx, final SocksMessage message) throws Exception {
        if (message instanceof Socks4CommandRequest) {
            final Socks4CommandRequest request = (Socks4CommandRequest) message;
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {
                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture = ctx.channel().writeAndFlush(
                                    new DefaultSocks4CommandResponse(Socks4CommandStatus.SUCCESS));
                            //成功建立连接，删除SocksServerConnectHandler，添加RelayHandler
                            responseFuture.addListener(channelFuture -> {
                                ctx.pipeline().remove(SocksServerConnectHandler.this);
                                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(
                                    new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED));
                            closeOnFlush(ctx.channel());
                        }
                    });

            Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientPromiseHandler(promise));

            b.connect(request.dstAddr(), request.dstPort()).addListener(future -> {
                if (future.isSuccess()) {
                    // 成功建立连接
                } else {
                    // 关闭连接
                    ctx.channel().writeAndFlush(
                            new DefaultSocks4CommandResponse(Socks4CommandStatus.REJECTED_OR_FAILED)
                    );
                    closeOnFlush(ctx.channel());
                }
            });
        } else if (message instanceof Socks5CommandRequest) {
            final Socks5CommandRequest request = (Socks5CommandRequest) message;
            Promise<Channel> promise = ctx.executor().newPromise();
            promise.addListener(
                    (FutureListener<Channel>) future -> {
                        final Channel outboundChannel = future.getNow();
                        if (future.isSuccess()) {
                            ChannelFuture responseFuture =
                                    ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                            Socks5CommandStatus.SUCCESS,
                                            request.dstAddrType(),
                                            request.dstAddr(),
                                            request.dstPort()));

                            //成功建立连接，删除SocksServerConnectHandler，添加RelayHandler
                            responseFuture.addListener(channelFuture -> {
                                ctx.pipeline().remove(SocksServerConnectHandler.this);
                                outboundChannel.pipeline().addLast(new RelayHandler(ctx.channel()));
                                ctx.pipeline().addLast(new RelayHandler(outboundChannel));
                            });
                        } else {
                            ctx.channel().writeAndFlush(new DefaultSocks5CommandResponse(
                                    Socks5CommandStatus.FAILURE, request.dstAddrType()));
                            closeOnFlush(ctx.channel());
                        }
                    });

            final Channel inboundChannel = ctx.channel();
            b.group(inboundChannel.eventLoop())
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ClientPromiseHandler(promise));

            b.connect(request.dstAddr(), request.dstPort()).addListener( future -> {
                if (future.isSuccess()) {
                    // 成功建立连接
                } else {
                    // 关闭连接
                    ctx.channel().writeAndFlush(
                            new DefaultSocks5CommandResponse(Socks5CommandStatus.FAILURE, request.dstAddrType()));
                    closeOnFlush(ctx.channel());
                }
            });
        } else {
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        closeOnFlush(ctx.channel());
    }

    /**
     * 关闭channel
     */
    public  void closeOnFlush(Channel ch) {
        if (ch.isActive()) {
            ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
