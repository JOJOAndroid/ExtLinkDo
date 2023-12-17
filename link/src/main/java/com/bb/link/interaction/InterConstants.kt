package com.bb.link.interaction

import java.io.File

object InterConstants {
    const val FilePort = 7071
    const val MessagePort = 7070 //json格式的数据包

    const val Datatype_Text = "data_text"
    const val Datatype_File = "data_file"
    const val Datatype_Key  = "data_key"
    /**
     * TODO
     * 1.确认GO GC的创建机制
     * 2.确认json
     * 3.扫描功能 图片、音频格式、视频格式；扫描文件【扫描固定的目录】
     */
    //1。通过7073端口 客户端给服务端发信息 告知要发的文件类型、路径、文件名称【包括后缀：MP4】
    //2。服务端也通过7073端口 告知客户端 准备好
    //3。客户端发送文件
    //4。服务端收

    //14：Json 格式转成字符串和解压出来，TODO1搞定
    //15：aar
    //16：

    //7073 发送文本--需要存，本地存储；不需要，展示一次
    //7074 keyevent
    //7075 图片

//    {
//        type:7071\7072\7073\
//        client：
//        path:
//        filename:kk.apk
//        data:--文字
//        keyevent:
//        return:wiat/success/fail
//        deviceFile:
//    }

    /**
     * 需求：
     * 1.车机端APk展示、卸载、安装
     * 2.文件：要展示车机端文件目录、手机端文件目录，文件删除、拷贝
     * 3.方控按键下发
     *
     * ----推荐----
     * 增加服务器能力，提供外部资源
     */

    /**
     * {
    "type": "发送的文件类型：7071-音频，7072-视频，7073-文本，7074-文件，7075-方控指令",
    "key": "发送方随机生成密钥发给服务端，服务端拿着密钥解密后，返回客户端确保可以进行文件传送，并且商定传输端口；除了7073和7075不需要",
    "transferport":"由服务端返回指定传输使用的端口号",
    "transfercontinue":"客户端拿到服务端给的端口号进行传输，并且给传输动作true或者false，true表示还有文件端口继续使用，false表示没有更多的文件传输了，该端口释放",
    "data": {
    "path": "/example/path",
    "filename": "kk.apk",
    "text": "--文字",
    "keyevent": "exampleKeyEvent",
    "deviceFile": "exampleDeviceFile"
    },
    "serverstate": "wait/success/fail"
    }
     */
}