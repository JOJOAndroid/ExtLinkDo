package com.bb.link.interaction

import android.util.Log
import com.bb.link.manager.P2pManagerProxy
import com.bb.link.mode.FileBean
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.BindException
import java.net.ServerSocket

class FileReceiver () {
    private var serverSocket:ServerSocket ?= null
    var reReceiver : Boolean = false
    fun receiveFile(filePath: String,msgListener: P2pManagerProxy.P2pMsgListener?) {
        val destinationFile = File(filePath)
        if(destinationFile.exists()) {
            receiveFileInBackground(destinationFile,msgListener)
        }else {
            Log.e("lzp","文件不存在，处理相应逻辑：${destinationFile.absoluteFile}")
            try{
                val createResule = destinationFile.mkdirs()
                Log.e("lzp","创建:$createResule")
                if(createResule) {
                    receiveFileInBackground(destinationFile,msgListener)
                }
            }catch (e: java.lang.Exception) {
                Log.e("lzp","文件不存在，并且创建失败:$e")
            }
        }
    }

    private fun receiveFileInBackground(destinationFile: File,msgListener: P2pManagerProxy.P2pMsgListener?) {
        if (serverSocket == null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    serverSocket = ServerSocket(InterConstants.FilePort)
                    println("等待文件连接...")
                    reReceiver = true
                    while (reReceiver) {
                        // 在循环中接受新的连接请求
                        val clientSocket = serverSocket?.accept()
                        println("文件连接已建立")
                        try {
                            Log.e("lzp", "clientSocket:${clientSocket == null},connect:${clientSocket?.isConnected}")
                            val mInputStream = clientSocket?.getInputStream()
                            val objectInputStream: ObjectInputStream = ObjectInputStream(mInputStream)
                            val fileBean: FileBean = objectInputStream.readObject() as FileBean

                            val name:String  = File(fileBean.filePath).getName();
                            Log.e("lzp","name:$name")
                            val file = File(destinationFile, name)
                            val mFileOutputStream = FileOutputStream(file)
                            //开始接收文件
                            val bytes = ByteArray(1024)
                            var len: Int = 0
                            var total : Long  = 0
                            var progress : Int = 0
                            len = mInputStream?.read(bytes)!!
                            while (len != -1) {
                                mFileOutputStream.write(bytes, 0, len);
                                total += len;
                                progress = ((total * 100) / fileBean.fileLength).toInt()
                                len = mInputStream?.read(bytes)!!
                                Log.e("LZP", "文件接收进度: " + progress);
                            }

//                            DataInputStream(clientSocket?.getInputStream()).use { dis ->
//                                val fileName = dis.readUTF()
//                                val fileSize = dis.readLong()

//                                Log.e("lzp", "接收到文件: $fileName, 大小: $fileSize 字节")
//
//                                // 创建文件输出流
//                                val file = File(destinationFile, fileName)
//                                if(!file.exists()) {
//                                    file.createNewFile()
//                                }
//                                Log.e("lzp", "=================")
//                                val fos = FileOutputStream(file)
//                                BufferedOutputStream(fos).use { bos ->
//                                    // 接收文件内容
//                                    val buffer = ByteArray(4096)
//                                    var bytesRead: Int
//                                    var totalBytesRead: Long = 0
//
//                                    while (totalBytesRead < fileSize) {
//                                        bytesRead = dis.read(buffer)
//                                        if (bytesRead == -1) break
//                                        bos.write(buffer, 0, bytesRead)
//                                        totalBytesRead += bytesRead
//                                    }
//                                }

//                                Log.e("lzp", "文件接收完成，保存路径: ${file.absolutePath}")
                                msgListener?.onFileResult(true)
//                            }
                        } catch (e: IOException) {
                            Log.e("lzp", "IOException: $e")
                            msgListener?.onFileResult(false)
                        } finally {
                            // 关闭连接
                            clientSocket?.close()
                        }
                    }

                }catch (e: BindException) {
                    // 端口已经被占用
                    // 处理异常，选择其他端口或者通知用户
                    Log.e("lzp", "BindException: $e")
                    msgListener?.onFileResult(false)
                } catch (e: IOException) {
                    // 其他 I/O 异常
                    Log.e("lzp", "IOException--: $e")
                    msgListener?.onFileResult(false)
                } finally {
                    // 关闭服务器套接字
                    serverSocket?.close()
                }

            }
        }
    }

    fun colseSocket() {
        reReceiver = false
        serverSocket?.close()
    }
}