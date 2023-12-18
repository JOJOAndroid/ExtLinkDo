package com.lzp.extlinkdo


import android.annotation.SuppressLint
import android.content.Context
import android.net.wifi.p2p.*
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bb.link.interaction.InterConstants
import com.bb.link.manager.P2pManagerProxy
import com.bb.link.mode.BDMsg
import com.bb.link.mode.BDMsgData
import com.bb.link.mode.BDevice
import com.bb.link.util.PermissionUtil
import com.google.gson.Gson
import com.lzp.extlinkdo.adapter.DeviceListAdapter
import java.io.File

class P2PFileTransferActivity : AppCompatActivity(){

    private val TAG = "LZP"

    private lateinit var deviceListAdapter: DeviceListAdapter
    private val peers = mutableListOf<WifiP2pDevice>()



    private var p2pManagerProxy: P2pManagerProxy? = null
    private var p2pEnable = false

    private var devicesAddress: String = ""
    private var mConext: Context?= null
    private var mLinkType: Int = -1

    private var showMsgData : TextView ?= null

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mConext = this

        mLinkType = BuildConfig.linktype
        Log.d(TAG, "myLinkType:$mLinkType")

        if (PermissionUtil().requestP2PPermissions(this)) {
            init()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PermissionUtil().PERMISSIONS_REQUEST_CODE) {
            if (PermissionUtil().areAllPermissionsGranted(grantResults)) {
                // 所有权限都已经被授予，执行你的操作
                init()
            } else {
                // 权限被拒绝，根据需要进行处理
                Log.d(TAG, "权限被拒绝，根据需要进行处理")
            }
        }
    }

    /**
     * 发送=1
     * 发送成功=2
     */
    var fileSendStatus = 0
    var testFile1 = Environment.getExternalStorageDirectory().absolutePath+"/DCIM/shared.png"
    var testFile2 = Environment.getExternalStorageDirectory().absolutePath+"/DCIM/shared1.mp4"

    /**
     * 1 testFile1
     * 2 testFile2
     */
    var sendFileSwitch = 1
    private fun init() {
        //UI 初始化
        showMsgData = findViewById(R.id.show_msg_data)

        findViewById<TextView>(R.id.devicesName).text = Settings.Secure.getString(contentResolver, "bluetooth_name") ?: ""

        findViewById<TextView>(R.id.linkt_type).text =
            if(mLinkType == P2pManagerProxy.LinkTypeServer) "服务端" else "客户端"

        findViewById<TextView>(R.id.devices_update).setOnClickListener {
            p2pManagerProxy?.removeGroupDevice()
            p2pManagerProxy?.cancelConnect()
            startScarn()
            Toast.makeText(this,"开始扫描",Toast.LENGTH_SHORT).show()
        }

        findViewById<TextView>(R.id.send_key).setOnClickListener {
            sendKey(InterConstants.Datatype_Key,"","up")
        }

        findViewById<TextView>(R.id.send_media).setOnClickListener {
            if(fileSendStatus != 1) {
                fileSendStatus = 1
                if(sendFileSwitch == 1) {
                    sendFile(testFile1)
                    sendFileSwitch = 2
                }else if(sendFileSwitch == 2) {
                    sendFile(testFile2)
                    sendFileSwitch = 1
                }
            }else{
                Log.d("LZP","fileSendStatus:$fileSendStatus")
            }
        }
        findViewById<TextView>(R.id.send_media).setOnLongClickListener {
            fileSendStatus = 2
            false
        }

        findViewById<TextView>(R.id.send_txt).setOnClickListener {
            sendKey(InterConstants.Datatype_Text,  "","请在服务端的大哥们好好看看这段文字")
        }

        if(mLinkType == P2pManagerProxy.LinkTypeServer) {
            findViewById<LinearLayout>(R.id.client_send).visibility = View.GONE
        }else{
            findViewById<LinearLayout>(R.id.client_send).visibility = View.VISIBLE
        }

        //adapter
        deviceListAdapter =DeviceListAdapter(ArrayList(), object : DeviceListAdapter.OnDeviceClickListener {
            //正常连接
            override fun onDeviceClick(device: BDevice) {
                //TODO 关于设备的记录留存 要思考下怎么处理
                if (device.status != "0") {
                    p2pManagerProxy?.connectToDevice(device)
                }
            }

            //断连
            override fun onDeviceLongClick(device: BDevice) {
                if (device.status == "0") {
                    Log.d("LZP", "cancenl")
                    p2pManagerProxy?.removeGroupDevice()
                }
            }
        })
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = deviceListAdapter

        //P2P
        initP2pProxy()
    }

    private fun initP2pProxy() {
        //P2P 控件初始化
        p2pManagerProxy = P2pManagerProxy.instance

        p2pManagerProxy?.registerP2pListener(object: P2pManagerProxy.WifiP2pListener{
            override fun onConnectionChanged(connectState: Boolean, wifiP2pInfo: WifiP2pInfo?) {
                Log.d("LZP", "onConnectionChanged:$connectState")
                if (connectState) {
                    //连接上的设备
                    Log.d("LZP", wifiP2pInfo.toString())
                } else {
                    deviceListAdapter.clearUpdateDeviceList()
                }
            }

            override fun onDiscoverDevices(devices: List<BDevice>?) {
                Log.d("LZP", "onDiscoverDevices:")
                if (devices != null) {
                    p2pManagerProxy?.requestWifiP2pInfo()
                    deviceListAdapter.updateDeviceList(devices)
                }
            }

            override fun onWifiP2pEnable(enable: Boolean) {
                Log.d("LZP", "onWifiP2pEnable:$enable")
                p2pEnable = enable
                startScarn()
            }

            override fun onGroupOrClient(isGroup: Boolean) {
                if (isGroup) {
                    if(mLinkType == P2pManagerProxy.LinkTypeClient) {
                        Toast.makeText(mConext, "客户端注册了GO", Toast.LENGTH_SHORT).show()
                        P2pManagerProxy.instance.cancelConnect()
                        P2pManagerProxy.instance.removeGroupDevice()

                    }else {
                        Toast.makeText(mConext, "This is GO", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(mConext, "This is GC", Toast.LENGTH_SHORT).show()
                }
            }

        })

        p2pManagerProxy?.registerMsgListener(object: P2pManagerProxy.P2pMsgListener{
            override fun onMessgae(data: String) {
                showMsgData?.text = data.toString()
                val receivedMessage = Gson().fromJson(data, BDMsg::class.java)
            }

            override fun onFileResult(result: Boolean) {
                showMsgData?.text = "服务端接收文件状态：$result"
                fileSendStatus = 2
            }
        })

        if(mLinkType == P2pManagerProxy.LinkTypeServer) {
            p2pManagerProxy?.initP2pServerProxy(this, mainLooper)
            //创建服务端接受文件的路径
            val destinationFile = File(getExternalFilesDir(null),"A2A")
            if(!destinationFile.exists()) {
                destinationFile.mkdirs()
            }
            p2pManagerProxy?.registerReceiveFilePath(destinationFile.absolutePath)
        }else {
            p2pManagerProxy?.initP2pClientProxy(this, mainLooper)
        }
    }

    override fun onResume() {
        super.onResume()
        p2pManagerProxy?.requestPeers()
        p2pManagerProxy?.getGroupDevice()
    }

    override fun onPause() {
        super.onPause()
        p2pManagerProxy?.unRegisterDataPoint()
        p2pManagerProxy?.unRegisterReceiveFile()
    }

    private fun startScarn() {
        if (p2pEnable) {
            p2pManagerProxy?.discoverDevice()
        }
    }

    override fun onStop() {
        super.onStop()
    }

    /**
     * @param type 发送类型：keyevent、file、text
     * @param path 绝对路径，keyevent可忽略
     * @param fileName 文件名称，keyevent可忽略
     */
    private fun sendKey(type: String,text: String,keyevent:String) {
        // 创建消息对象
        val message = BDMsg(
            type,
            BDMsgData(
                text,
                keyevent
            )
        )

        // 将消息对象转换为 JSON 字符串
        val jsonMessage = Gson().toJson(message)
        // 在实际应用中，您将通过网络或其他方式发送 JSON 字符串给接收方
        showMsgData?.text = jsonMessage
        p2pManagerProxy?.sendJsonData(jsonMessage,InterConstants.MessagePort)
    }

    private fun sendFile(path: String) {
        Log.d("LZP","-----------------？？？？？？？？？path:$path,InterConstants.FilePort:${InterConstants.FilePort}")
        p2pManagerProxy?.sendFile(path,InterConstants.FilePort)
    }
}
