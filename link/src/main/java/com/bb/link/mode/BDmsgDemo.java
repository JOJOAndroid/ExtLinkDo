package com.bb.link.mode;

import com.google.gson.Gson;

public class BDmsgDemo {

    public void sendDemo() {
        // 创建消息对象
        BDMsg message = new BDMsg(
                "7071",
                "randomGeneratedKey",
                "randomGeneratedPort",
                true,
                new BDMsgData("/example/path", "kk.apk", "--文字", "exampleKeyEvent", "exampleDeviceFile"),
                "wait"
        );


        // 将消息对象转换为 JSON 字符串
        String jsonMessage = new Gson().toJson(message);

        // 在实际应用中，您将通过网络或其他方式发送 JSON 字符串给接收方
        System.out.println("发送消息：" + jsonMessage);
    }

    public void receiverDemo(String msg) {
        BDMsg receivedMessage = new Gson().fromJson(msg, BDMsg.class);
    }

}
