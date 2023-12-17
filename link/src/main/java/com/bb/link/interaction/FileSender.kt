package com.bb.link.interaction

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.InetSocketAddress
import java.net.Socket

class FileSender(private val filePath: String, private val ipAddress: String, private val port: Int) {

    private var clientSocket: Socket?= null
    fun sendFile() {
        Log.d("LZP","文件path:-----------------------------------")
        GlobalScope.launch(Dispatchers.IO) {
            if (filePath.isNotEmpty()) {
                val file = File(filePath)
                Log.d("LZP","文件path:$filePath")
                if(file.exists()) {
                    val fileSize = file.length().toInt()
                    try {
                        clientSocket = Socket()
                        clientSocket.use { socket ->
                            socket?.connect(InetSocketAddress(ipAddress, port), 5000)
                            Log.d("LZP","send1")
                            // 发送文件名和文件大小
//                            DataOutputStream(socket?.getOutputStream()).use { dos ->
//                                dos.writeUTF(file.name)
//                                dos.writeInt(fileSize)
//                            }
                            Log.d("LZP","send2")
                            if(socket != null && socket.isConnected) {
                                // 发送文件内容
                                BufferedInputStream(FileInputStream(file)).use { bis ->
                                    DataOutputStream(socket?.getOutputStream()).use { dos ->
                                        val buffer = ByteArray(4096)
                                        var bytesRead: Int

                                        while (bis.read(buffer).also { bytesRead = it } != -1) {
                                            dos.write(buffer, 0, bytesRead)
                                        }
                                    }
                                }
                            }else {
                                Log.d("LZP","socket 断开了")
                            }
                            Log.d("LZP","文件发送完成")
                        }
                    }catch (e: Exception) {
                        e.printStackTrace()
                    }finally {
//                        try {
//                            clientSocket?.close()
//                        } catch (e: IOException) {
//                            e.printStackTrace()
//                        }
                    }
                }else {
                    Log.d("LZP","File not found:")
                }
            }else {
                Log.d("LZP","File not found:")
            }
        }
    }

    fun colseSocket() {
//        clientSocket?.close()
    }
}