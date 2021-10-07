package com.github.salpadding.jal.netty

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel

class NettyServer(val port: Int) {
    // start listening on port
    fun start() {
        // boss group, accept clients
        val bossGroup = NioEventLoopGroup()

        // handle socket read/write
        val workerGroup = NioEventLoopGroup()

        val bootstrap = ServerBootstrap()

        val handler = object : ChannelInitializer<SocketChannel>() {
            override fun initChannel(ch: SocketChannel) {
                println("init channel called")
                ch.pipeline().addLast(NettyServerHandler())
            }
        }


        bootstrap.group(bossGroup, workerGroup)
            // select channel implementation
            .channel(NioServerSocketChannel::class.java)
            // limit max size of pending connections
            .option(ChannelOption.SO_BACKLOG, 128)
            // set keep alive flag
            .childOption(ChannelOption.SO_KEEPALIVE, true)
            .childHandler(handler)

        // wait for bind
        val future = bootstrap.bind(port).sync()

        // blocking until server closed
        future.channel().closeFuture().sync()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val s = NettyServer(8080)
            s.start()
        }
    }
}

class NettyServerHandler : ChannelInboundHandlerAdapter() {
    private val buffer = ByteArray(BUF_SIZE)

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        // get pipeline by context ctx.pipeline()
        // get channel by context ctx.channel()
        val buf = msg as ByteBuf
        println("channelRead() called, ctx = $ctx")
        println("client address = ${ctx.channel().remoteAddress()}")
        println("message from client:")

        synchronized(buffer) {
            var cur = buf.readerIndex()

            while (cur < buf.writerIndex()) {
                val len = Math.min(BUF_SIZE, buf.writerIndex() - cur)
                buf.getBytes(cur, buffer, 0, len)
                System.out.write(buffer, 0, len)
                cur += len
            }
        }

        // echo message
        ctx.writeAndFlush(buf)
    }


    // this method called when channel read completed
    override fun channelReadComplete(ctx: ChannelHandlerContext) {

    }

    companion object {
        const val BUF_SIZE = 4096
    }
}