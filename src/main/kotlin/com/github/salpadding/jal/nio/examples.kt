package com.github.salpadding.jal.nio

import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer

class FileWriteExample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val content = "hello world\n"
            val os = FileOutputStream("file.txt")

            // create file channel by output stream
            val buf = ByteBuffer.allocate(4096)
            buf.put(content.toByteArray())

            // set limit = position
            // position = 0
            buf.flip()
            os.channel.write(buf)
            os.close()
        }
    }
}

class FileReadExample {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val ins = FileInputStream("README.MD")
            val buf = ByteBuffer.allocate(4)

            while (true) {
                buf.clear()
                val n = ins.channel.read(buf)
                if (n < 0) {
                    break
                }
                System.out.write(buf.array(), 0, n)
            }
        }
    }
}

class FileCopyExample {
    companion object {
        const val SRC = "README.MD"
        const val DST = "README.MD.bak"

        @JvmStatic
        fun main(args: Array<String>) {
            val ins = FileInputStream(SRC)
            val os = FileOutputStream(DST)
            val buf = ByteBuffer.allocate(8)

            while(true) {
                buf.clear()
                val n = ins.channel.read(buf)
                buf.flip()
                os.channel.write(buf)

                if(n < 0) {
                    break
                }
            }

            os.close()
            ins.close()
        }
    }
}

