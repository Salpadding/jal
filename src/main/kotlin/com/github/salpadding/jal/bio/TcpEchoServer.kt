package com.github.salpadding.jal.bio

import java.net.ServerSocket
import java.util.concurrent.Executors

class TcpEchoServer {
    private val ex = Executors.newCachedThreadPool()
    private var port = 8080

    fun init(port: Int) {
        this.port = port
    }

    fun start() {
        // create stream socket and bind;
        val sock = ServerSocket(port)
        println("server is listening on ${port}")

        // wait for clients
        while (true) {
            // block until new client connected
            val client = sock.accept()
            println("a new client connected remote ip = ${client.inetAddress.hostAddress} port = ${client.port}")

            // create a new thread, handle this client
            ex.execute {
                client.use {
                    val buf = ByteArray(BUF_SIZE)

                    // read from socket
                    val st = it.getInputStream()

                    while (true) {
                        // read data from client
                        val n = st.read(buf)

                        // remote closed or eof
                        if (n <= 0) {
                            break
                        }

                        // n > 0, print to stdout
                        println("message from client:")
                        System.out.write(buf, 0, n)

                        it.getOutputStream().write(buf, 0, n)
                    }
                }

                println("connection to $client closed")
            }
        }
    }

    companion object {
        // buffer size
        private const val BUF_SIZE = 4096

        @JvmStatic
        fun main(args: Array<String>) {
            val server = TcpEchoServer()
            server.start()
        }
    }
}

