package com.bb.link.manager

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.MacAddress
import android.net.NetworkInfo
import android.net.wifi.p2p.*
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import com.bb.link.interaction.*
import com.bb.link.mode.BDevice
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class P2pManagerProxy private constructor() : BroadcastReceiver(),IP2pManagerProxy{

    private val TAG = "P2pManagerProxy"
    private lateinit var wifiP2pManager: WifiP2pManager
    private lateinit var channel: WifiP2pManager.Channel

    var linkType = -1

    companion object {
        val LinkTypeServer = 1
        val LinkTypeClient = 2
        val instance : P2pManagerProxy by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
            P2pManagerProxy()
        }
    }

    init {

    }

    override fun initP2pClientProxy(context: Context, myLooper: Looper) {
        initP2pProxy(context,myLooper)
        linkType = LinkTypeClient
    }

    override fun initP2pServerProxy(context: Context, myLooper: Looper) {
        initP2pProxy(context,myLooper)
        linkType = LinkTypeServer
        createGO()
    }

    private fun initP2pProxy(context: Context, myLooper: Looper) {
        wifiP2pManager = context.getSystemService(Context.WIFI_P2P_SERVICE) as WifiP2pManager
        channel = wifiP2pManager.initialize(context, myLooper, null)

        context.registerReceiver(this, getP2PIntentFilter())
    }

    @SuppressLint("MissingPermission")
    override fun createGO() {
        wifiP2pManager.createGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // Device is ready to accept incoming connections from peers.
                Log.d(TAG,"创建群组成功")
                RegisterReceiveFile(Environment.getExternalStorageDirectory().absolutePath+"/P2P/")
            }

            override fun onFailure(reasonCode: Int) {
                if (reasonCode == 0) {
                    // 通常，reasonCode 为 0 表示成功，因此在这里可能是正常情况
                } else {
                    // 群组创建失败，处理其他失败原因
                    Log.d(TAG,"创建群组失败${reasonCode}")
                }
            }
        })
    }

    @SuppressLint("MissingPermission")
    override fun discoverDevice() {
        cancelConnect()
        wifiP2pManager.discoverPeers(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 发现对等设备成功
                Log.d(TAG,"discover-onSuccess:")
            }

            override fun onFailure(reasonCode: Int) {
                // 发现对等设备失败
                Log.d(TAG,"discover-onFailure:${reasonCode}")
            }
        })
    }

    override fun stopDiscoveryDevice() {
        wifiP2pManager.stopPeerDiscovery(channel, null)
    }

    @SuppressLint("MissingPermission")
    override fun connectToDevice(device: BDevice) {
        val config = WifiP2pConfig()
        config.deviceAddress = device.address
        if(linkType == LinkTypeServer) {
            config.groupOwnerIntent = 15
        }else {
            config.groupOwnerIntent = 0
        }

        wifiP2pManager.connect(channel, config, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 连接成功
            }

            override fun onFailure(reasonCode: Int) {
                Log.d(TAG,"connect onFailure--${reasonCode}")
                when (reasonCode) {
                    // 连接失败
                    WifiP2pManager.BUSY -> {
                        // Wi-Fi P2P 框架正忙，无法执行操作
                    }
                    WifiP2pManager.ERROR -> {
                        // 发生一般性错误
                        cancelConnect()
                        removeGroupDevice()
                        GlobalScope.launch {
                            // 延迟 1 秒
                            delay(1000)

                            // 在延迟后执行的操作
                            println("延迟 1 秒后执行的操作")
                            if(linkType == LinkTypeServer) {
                                createGO()
                            }
                        }
                    }
                    WifiP2pManager.P2P_UNSUPPORTED -> {
                        // 设备不支持 Wi-Fi P2P
                    }
                    WifiP2pManager.NO_SERVICE_REQUESTS -> {
                        // 没有可用的服务请求
                    }
                    // 可以根据实际情况添加其他失败原因的处理
                    else -> {
                        // 未知的失败原因
                    }
                }
            }
        })

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun disconnectClients(device: BDevice) {
        //此方法存在版本不适用情况
//        wifiP2pManager.removeClient(channel, MacAddress.fromString(device.address),object : WifiP2pManager.ActionListener {
//            override fun onSuccess() {
//            }
//
//            override fun onFailure(p0: Int) {
//                Log.d(TAG,"disconnect devices--${p0}")
//            }
//        })
    }

    override fun getGroupDevice() {
        wifiP2pManager.requestGroupInfo(channel
        ) { group ->
            group?.let {
                // 获取连接信息
                val networkName: String = group.networkName
                val passphrase: String = group.passphrase
                // 处理连接信息
                Log.d(TAG,"networkName--${networkName}")
                Log.d(TAG,"passphrase--${passphrase}")
            }
        }

    }

    override fun removeGroupDevice() {
        wifiP2pManager.removeGroup(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 连接断开成功
            }

            override fun onFailure(reasonCode: Int) {
                // 连接断开失败
                Log.d(TAG,"disconnect devices--${reasonCode}")
            }
        })
    }

    fun cancelConnect() {
        // 取消连接
        wifiP2pManager.cancelConnect(channel, object : WifiP2pManager.ActionListener {
            override fun onSuccess() {
                // 取消连接成功
            }

            override fun onFailure(reason: Int) {
                // 取消连接失败，根据 reason 进行处理
                Log.e(TAG,"cancelConnect devices--${reason}")
            }
        })
    }

    private fun getP2PIntentFilter(): IntentFilter {
        val intentFilter = IntentFilter()
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION) // Wi-Fi P2P 状态变化的广播事件
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION) // Wi-Fi P2P 设备列表变化的广播事件
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION) //Wi-Fi P2P 连接状态发生变化
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION) //当本设备的信息发生变化
        return intentFilter
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d(TAG,"intent?.action:${intent?.action}")
        when (intent?.action) {
            WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                // 检查Wi-Fi P2P是否可用
                val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                listener?.onWifiP2pEnable(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED)
            }
            WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION -> {
                // 对等设备列表发生变化
                requestPeers()
            }
            WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> {
                // Wi-Fi P2P 连接状态发生变化
                val networkInfo = intent.getParcelableExtra<android.net.NetworkInfo>(WifiP2pManager.EXTRA_NETWORK_INFO)

                if (networkInfo?.isConnected == true) {
                    listener?.onConnectionChanged(true,null)
                    requestWifiP2pInfo()
                    if(linkType == LinkTypeServer) {
                        registerPoint()
                    }
                }else {
                    listener?.onConnectionChanged(false,null)
                    if(linkType == LinkTypeServer) {
                        unRegisterPoint()
                    }
                }
            }
            WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION -> {
                // 本设备信息发生变化
                val device: WifiP2pDevice? =
                    intent.getParcelableExtra(WifiP2pManager.EXTRA_WIFI_P2P_DEVICE)

                if (device != null) {
                    // 处理本设备信息变化
                    // 处理本设备信息的变化
                    // 可以获取设备的名称、地址等信息
                    val deviceName = device.deviceName
                    val deviceAddress = device.deviceAddress
                    val state =device.status

                    Log.d("LZP","$deviceName,$deviceAddress,${device.status},${linkType == LinkTypeServer}")
                    if(state == 0 && linkType == LinkTypeServer) {
                        registerPoint()
                    }
                }
            }
        }
    }

    // 请求设备列表
    fun requestPeers() {
        wifiP2pManager.requestPeers(channel
        ) { peers -> // 更新 RecyclerView 中的设备列表
            updateDeviceList(peers.deviceList)
        }
    }

    // 更新 RecyclerView 中的设备列表
    private fun updateDeviceList(deviceList: Collection<WifiP2pDevice>) {
        val devices = deviceList.map { BDevice(it.deviceName, it.deviceAddress,it.status.toString()) }
        if(devices!=null && devices.isNotEmpty()) {
            listener?.onDiscoverDevices(devices)
        }
    }

    var hostAddress : String = ""
    fun requestWifiP2pInfo() {
        // 连接成功
        wifiP2pManager.requestConnectionInfo(channel) { info: WifiP2pInfo ->

            if(info.groupFormed) {
                hostAddress = info.groupOwnerAddress.hostAddress
            }

            if (info.groupFormed && info.isGroupOwner) {
                listener?.onGroupOrClient(true)
            } else if (info.groupFormed) {
                listener?.onGroupOrClient(false)
            }
        }
    }

    private var listener: WifiP2pListener ?= null
    // 定义接口
    interface WifiP2pListener {
        fun onConnectionChanged(connectState:Boolean,wifiP2pInfo: WifiP2pInfo?)
        fun onDiscoverDevices(devices: List<BDevice>?)
        fun onWifiP2pEnable(enable: Boolean)
        fun onGroupOrClient(isGroup: Boolean)
    }

    fun registerP2pListener(listener:WifiP2pListener) {
        this.listener = listener
    }

    private var msgListener: P2pMsgListener ?= null
    interface P2pMsgListener {
        fun onMessgae(data: String)
        fun onFileResult(result: Boolean)
    }
    fun registerMsgListener(listener:P2pMsgListener) {
        this.msgListener = listener
    }

    private var dataSender: DataSender? = null
    private var fileReceiver: FileReceiver? = null
    private var registerPoint: RegisterPoint? = null

    fun RegisterReceiveFile(path : String) {
        // 在这里添加接收文件的代码
        if(fileReceiver == null) {
            fileReceiver = FileReceiver()
            fileReceiver?.receiveFile(path,msgListener)
        }
    }

    private fun registerPoint() {
        if(registerPoint == null) {
            registerPoint = RegisterPoint()
        }
        registerPoint?.receiveFileInBackground(msgListener)
    }

    private fun unRegisterPoint() {
        registerPoint?.colseSocket()
    }

    fun sendJsonData(data: String,point: Int) {
        Handler().postDelayed({
            // 在这里添加发送文件的代码
            dataSender = hostAddress?.let {
                DataSender(data,
                    it, point)
            }
            dataSender?.sendFile()
        },100)
    }
}