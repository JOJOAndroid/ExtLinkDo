package com.bb.link.interaction

import android.util.Log
import com.bb.link.manager.P2pManagerProxy
import com.bb.link.mode.BDMsg
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStreamReader
import java.net.BindException
import java.net.ServerSocket

class DataReceiver () {
    var serverSocket:ServerSocket ?= null
    var reReceiver : Boolean = false
    fun receiveFileInBackground(listener: P2pManagerProxy.P2pMsgListener?) {
        println("wiat==============${serverSocket == null}")
        if(serverSocket == null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    serverSocket = ServerSocket(InterConstants.MessagePort)
                    println("wiat==============")
                    reReceiver = true
                    // 端口可用，可以绑定
                    while (reReceiver) {
                        val clientSocket = serverSocket?.accept()
                        println("wiat==============accept")
                        try {
                            BufferedReader(InputStreamReader(clientSocket?.getInputStream())).use { reader ->
                                val jsonData = reader.readLine()
                                val receivedMessage =
                                    Gson().fromJson(jsonData, BDMsg::class.java)
                                when (receivedMessage.type) {
                                    InterConstants.Datatype_File -> {

                                    }

                                    InterConstants.Datatype_Text -> {
                                        listener?.onMessgae(jsonData)
                                    }

                                    InterConstants.Datatype_Key -> {
                                        listener?.onMessgae(jsonData)
                                    }

                                    else -> {
                                        Log.e("RegisterPoint", "发送端类型未在定义范围")
                                        return@launch
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            Log.e("lzp", "IOException: $e")
                        } finally {
                            // 关闭连接
                            clientSocket?.close()
                        }
                    }

                } catch (e: BindException) {
                    // 端口已经被占用
                    // 处理异常，选择其他端口或者通知用户
                    Log.e("lzp","BindException:$e")
                } catch (e: IOException) {
                    // 其他 I/O 异常
                    Log.e("lzp","IOException:$e")
                } finally {
                    // 关闭服务器套接字
                    serverSocket?.close()
                }
            }

        }else {
//            serverSocket?.close()
        }
    }

    fun colseSocket() {
        reReceiver = false
        serverSocket?.close()
    }
}