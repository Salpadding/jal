package com.github.salpadding.jal.nio

import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.concurrent.Executors

class TcpEchoServer(private val port: Int) {
    private val executor = Executors.newCachedThreadPool()

    fun start() {
        // create a socket channel
        val ch = ServerSocketChannel.open()
        val addr = InetSocketAddress(port)
        // bind to addr
        ch.bind(addr)
        println("server is listen on $port")

        while (true) {
            val client = ch.accept()

            println("new connection...")
            handle(client)
        }
    }

    private fun handle(c: SocketChannel) {
        // print client input, and echo
        val buf = ByteBuffer.allocate(BUF_SIZE)

        executor.execute {
            while (true) {
                buf.clear()
                val n = c.read(buf)
                if (n <= 0)
                    break
                System.out.write(buf.array(), 0, n)
                buf.flip()
                c.write(buf)
            }
        }
    }

    companion object {
        const val BUF_SIZE = 256

        @JvmStatic
        fun main(args: Array<String>) {
            val s = TcpEchoServer(8080)
            s.start()
        }
    }
}

class TcpSelector(val port: Int) {
    fun start() {
        // create a socket
        val ch = ServerSocketChannel.open()
        // bind to port
        val addr = InetSocketAddress(port)
        ch.bind(addr)
        // enable nio
        ch.configureBlocking(false)

        // create selector
        val selector = Selector.open()
        println("server is listen on $port")

        ch.register(selector, SelectionKey.OP_ACCEPT)

        while (!Thread.currentThread().isInterrupted) {
            // block until event emit
            val n = selector.select()

            println("n = $n")
            val keys = selector.selectedKeys().iterator()

            while (keys.hasNext()) {
                val k = keys.next()

                if (k.isAcceptable) {
                    val client = (k.channel() as ServerSocketChannel).accept()

                    println("new connection ${client.remoteAddress} ${client.socket().port}")
                    client.configureBlocking(false)

                    // provide a byte buffer
                    client.register(
                        selector, SelectionKey.OP_READ, ByteBuffer.allocate(
                            BUF_SIZE
                        )
                    )
                }

                if (k.isReadable) {
                    val client = k.channel() as SocketChannel
                    val buf = k.attachment() as ByteBuffer
                    val len = client.read(buf)

                    if (len > 0) {
                        // unregister
                        buf.flip()
                        System.out.write(buf.array(), 0, len)
                        client.write(buf)
                    } else if (len < 0) {
                        println("close remote")
                        k.channel().close()
                    }
                }
                // remove
                keys.remove()
            }
        }
    }

    companion object {
        const val BUF_SIZE = 256

        @JvmStatic
        fun main(args: Array<String>) {
            val s = TcpSelector(8080)
            s.start()
        }
    }
}

class TcpClient(val host: String, val port: Int) {
    private val ch = SocketChannel.open()
    private val out = ByteBuffer.allocate(1024)

    fun send(msg: String) {
        out.clear()
        out.put(msg.toByteArray())
        out.flip()
        ch.write(out)
    }

    fun start() {
        val remote = InetSocketAddress(host, port)
        ch.connect(remote)
        ch.configureBlocking(false)
        val selector = Selector.open()
        ch.register(selector, SelectionKey.OP_READ, ByteBuffer.allocate(1024))

        while (true) {
            selector.select()

            val iter = selector.selectedKeys().iterator()

            while (iter.hasNext()) {
                val event = iter.next()


                if (event.isReadable) {
                    val c = event.channel() as SocketChannel
                    val buf = event.attachment() as ByteBuffer
                    buf.clear()

                    val n = c.read(buf)

                    if (n <= 0) {
                        c.close()
                    } else {
                        buf.flip()
                        System.out.write(buf.array(), 0, n)
                    }
                }

                iter.remove()
            }
        }
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ex = Executors.newSingleThreadExecutor()
            val client = TcpClient("127.0.0.1", 8080)

            ex.execute {
                client.start()
            }

            while (true) {
                Thread.sleep(3000)
                client.send("hello world\n")
            }
        }
    }
}