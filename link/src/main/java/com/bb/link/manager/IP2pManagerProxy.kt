package com.bb.link.manager

import android.content.Context
import android.net.wifi.p2p.WifiP2pDevice
import android.os.Looper
import com.bb.link.mode.BDevice

interface IP2pManagerProxy {

    fun initP2pClientProxy(context: Context, myLooper: Looper)

    fun initP2pServerProxy(context: Context, myLooper: Looper)

    /**
     * 创建GO
     */
    fun createGO()

    /**
     * 发现设备
     */
    fun discoverDevice()

    /**
     * 停止设备发现搜索
     */
    fun stopDiscoveryDevice()

    /**
     * 已保存的群组
     */
    fun getGroupDevice()

    /**
     * 移除群组
     */
    fun removeGroupDevice()

    /**
     * 连接设备
     */
    fun connectToDevice(device: BDevice)

    /**
     * 和某个固定的设备断开
     */
    fun disconnectClients(device: BDevice)

}