package com.bb.link.interaction

import android.util.Log
import com.bb.link.manager.P2pManagerProxy
import com.bb.link.mode.BDMsg
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.*
import java.net.BindException
import java.net.ServerSocket

class RegisterPoint {
    var serverSocket:ServerSocket ?= null

    fun receiveFileInBackground(listener: P2pManagerProxy.P2pMsgListener?) {
        if(serverSocket == null) {
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    serverSocket = ServerSocket(InterConstants.MessagePort)
                    println("wiat==============")
                    // 端口可用，可以绑定
                    serverSocket.use { serverSocket ->
                        serverSocket!!.accept().use { clientSocket ->
                            BufferedReader(InputStreamReader(clientSocket.getInputStream())).use { reader ->
                                val jsonData = reader.readLine()
                                val receivedMessage = Gson().fromJson(jsonData, BDMsg::class.java)
                                when(receivedMessage.type) {
                                    InterConstants.Datatype_File -> {

                                    }
                                    InterConstants.Datatype_Text -> {
                                        listener?.onMessgae(jsonData)
                                    }
                                    InterConstants.Datatype_Key -> {
                                        listener?.onMessgae(jsonData)
                                    }else ->{
                                        Log.e("RegisterPoint","发送端类型未在定义范围")
                                        return@launch
                                    }
                                }

                            }
                        }
                    }
                } catch (e: BindException) {
                    // 端口已经被占用
                    // 处理异常，选择其他端口或者通知用户
                    Log.e("lzp","BindException:$e")
                } catch (e: IOException) {
                    // 其他 I/O 异常
                    Log.e("lzp","IOException:$e")
                }
            }

        }else {
//            serverSocket?.close()
        }
    }

    fun colseSocket() {
        serverSocket?.close()
    }
}