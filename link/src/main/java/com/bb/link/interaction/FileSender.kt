package com.bb.link.interaction

import android.util.Log
import com.bb.link.mode.FileBean
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
                            val outputStream: OutputStream? = socket?.getOutputStream()
                            val objectOutputStream = ObjectOutputStream(outputStream)


                            val fileBean = FileBean(filePath,file.length(),"001")
                            objectOutputStream.writeObject(fileBean)


                            val inputStream  = FileInputStream(file);
                            val size:Long  = fileBean.fileLength;
                            var total:Long  = 0
                            val bytes= ByteArray(1024)
                            var len = 0

                            len = inputStream.read(bytes)

                            while (len != -1) {
                                outputStream?.write(bytes, 0, len);
                                total += len;
                                val progress : Long = (total * 100) / size
                                len = inputStream.read(bytes)
                                Log.e("LZP", "文件发送进度：$progress");
                            }
                            outputStream?.close();
                            objectOutputStream.close();
                            inputStream.close();
                            socket?.close();
                            Log.d("LZP","文件发送完成")
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
                    Log.d("LZP","File not found:")
                }
            }else {
                Log.d("LZP","File not found:")
            }
        }
    }

    fun colseSocket() {
        clientSocket?.close()
    }
}