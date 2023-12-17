package com.lzp.extlinkdo.adapter

import android.net.wifi.p2p.WifiP2pDevice
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bb.link.mode.BDevice
import com.lzp.extlinkdo.R

class DeviceListAdapter(private var deviceList: List<BDevice>,
                        private val onDeviceClickListener: OnDeviceClickListener
) :
    RecyclerView.Adapter<DeviceListAdapter.MViewHolder>() {

    interface OnDeviceClickListener {
        fun onDeviceClick(device: BDevice)
        fun onDeviceLongClick(device: BDevice)
    }

    fun updateDeviceList(devices: List<BDevice>) {
        Log.d("LZP","展示")
        deviceList = devices
        notifyDataSetChanged()
    }

    fun clearUpdateDeviceList() {
        val nullList : List<BDevice> = arrayListOf()
        deviceList = nullList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_device, parent, false)
        return MViewHolder(view)
    }

    override fun onBindViewHolder(holder: MViewHolder, position: Int) {
        val device = deviceList[position]
        holder.bind(device, onDeviceClickListener)
    }

    override fun getItemCount(): Int {
        return deviceList.size
    }

    class MViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val deviceNameTextView: TextView = itemView.findViewById(R.id.deviceNameTextView)
        private val deviceStatusTextView: TextView = itemView.findViewById(R.id.deviceStatusTextView)

        fun bind(device: BDevice, clickListener: OnDeviceClickListener) {
            deviceNameTextView.text = device.name

            var statusText = ""
            when(device.status) {
                "0"->{
                    statusText = "已连接"
                }
                "1"->{
                    statusText = "正在连接"
                }
                "2"->{
                    statusText = "失败"
                }
                "3"->{
                    statusText = "可用"
                }
                "4"->{
                    statusText = "不可用"
                }
            }
            deviceStatusTextView.text = statusText
            // 在点击列表项时调用外部类的 connectToDevice 方法
            itemView.setOnClickListener {
                clickListener?.onDeviceClick(device)
            }
            itemView.setOnLongClickListener {
                clickListener?.onDeviceLongClick(device)
                false
            }
        }
    }
}