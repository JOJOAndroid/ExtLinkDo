package com.bb.link.interaction

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class DataSender(private val data: String, private val ipAddress: String, private val port: Int) {
    var clientSocket: Socket?= null
    fun sendFile() {
        GlobalScope.launch(Dispatchers.IO) {

            if (data.isNotEmpty()) {
                try {
                    if(clientSocket == null) {
                        clientSocket = Socket()
                        clientSocket.use { socket ->
                            socket?.connect(InetSocketAddress(ipAddress, port), 5000)
                            val writer = OutputStreamWriter(socket?.getOutputStream())
                            writer.write(data)
                            writer.flush()
                        }

                    }else {
                        clientSocket.use { socket ->
                            val writer = OutputStreamWriter(socket?.getOutputStream())
                            writer.write(data)
                            writer.flush()
                        }
                    }
                }catch (e: Exception) {
                    e.printStackTrace()
                }finally {

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