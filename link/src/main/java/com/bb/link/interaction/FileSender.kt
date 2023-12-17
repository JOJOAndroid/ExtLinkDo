package com.bb.link.interaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class FileSender(private val data: String, private val ipAddress: String, private val port: Int) {
    var clientSocket: Socket?= null
    fun sendFile() {
        GlobalScope.launch(Dispatchers.IO) {

            if (data.isNotEmpty()) {
                try {
                    clientSocket = Socket()
                    clientSocket.use { socket ->
                        socket?.connect(InetSocketAddress(ipAddress, port), 5000)

                        val writer = OutputStreamWriter(socket?.getOutputStream())
                        writer.write(data)
                        writer.flush()
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }finally {
                    try {
                        clientSocket?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }else {
                println("File not found:")
            }
        }
    }

    fun colseSocket() {
        clientSocket?.close()
    }
}