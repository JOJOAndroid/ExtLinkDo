package com.bb.link.interaction

import android.util.Log
import com.bb.link.manager.P2pManagerProxy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.BindException
import java.net.ServerSocket

class FileReceiver () {
    var serverSocket:ServerSocket ?= null

    fun receiveFile(filePath: String,msgListener: P2pManagerProxy.P2pMsgListener?) {
        val destinationFile = File(filePath)
        if(destinationFile.exists()) {
            GlobalScope.launch(Dispatchers.IO) {
                receiveFileInBackground(destinationFile,msgListener)
            }
        }else {
            Log.e("lzp","文件不存在，处理相应逻辑：$filePath")
            try{
                val createResule = destinationFile.createNewFile()
                Log.e("lzp","创建:$createResule")
                if(createResule) {
                    GlobalScope.launch(Dispatchers.IO) {
                        receiveFileInBackground(destinationFile,msgListener)
                    }
                }
            }catch (e: java.lang.Exception) {
                Log.e("lzp","文件不存在，并且创建失败:$e")
            }
        }
    }

    private fun receiveFileInBackground(destinationFile: File,msgListener: P2pManagerProxy.P2pMsgListener?) {
        if(serverSocket == null) {
            try {

                serverSocket = ServerSocket(InterConstants.FilePort)
                // 端口可用，可以绑定
                serverSocket.use { serverSocket ->
                    serverSocket!!.accept().use { clientSocket ->
                        BufferedInputStream(clientSocket.getInputStream()).use { inputStream ->
                            FileOutputStream(destinationFile).use { fileOutputStream ->
                                val buffer = ByteArray(1024)
                                var bytesRead: Int

                                while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                                    fileOutputStream.write(buffer, 0, bytesRead)
                                }

                                fileOutputStream.flush()
                                msgListener?.onFileResult(true)
                                Log.d("lzp","wanshile")
                                serverSocket.close()
                            }
                        }
                    }
                }
            } catch (e: BindException) {
                // 端口已经被占用
                // 处理异常，选择其他端口或者通知用户
                Log.e("lzp","BindException:$e")
                msgListener?.onFileResult(false)
            } catch (e: IOException) {
                // 其他 I/O 异常
                Log.e("lzp","IOException:$e")
                msgListener?.onFileResult(false)
            }
        }else {
            serverSocket?.close()
        }
    }

    fun colseSocket() {
        serverSocket?.close()
    }
}