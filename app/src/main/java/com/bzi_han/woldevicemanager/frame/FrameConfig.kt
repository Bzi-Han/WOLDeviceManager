package com.bzi_han.woldevicemanager.frame

import android.app.AlertDialog
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bzi_han.woldevicemanager.MainActivity
import com.bzi_han.woldevicemanager.R
import com.bzi_han.woldevicemanager.adapter.LanDeviceInfoAdapter
import com.bzi_han.woldevicemanager.dialog.DialogAddConfig
import com.bzi_han.woldevicemanager.entity.LanDeviceInfo
import com.bzi_han.woldevicemanager.utils.DeviceDiscovery
import com.bzi_han.woldevicemanager.utils.DeviceWakeUp

class FrameConfig : Fragment() {
    private var thisView: View? = null
    private var listView: ListView? = null
    private val handler = Handler()

    fun runOnUiThread(execution: () -> Unit) {
        handler.post(execution)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        thisView = inflater.inflate(R.layout.activity_frame_config, container, false)
        listView = thisView!!.findViewById(R.id.frame_config_listView)

        listView!!.adapter = LanDeviceInfoAdapter(ArrayList(), LayoutInflater.from(thisView!!.context)) {
            val lanDevice = ((it.parent as View).tag as LanDeviceInfoAdapter.ViewHolder).getData()

            lanDevice?.run {
                val sendWakeUp = {
                    val settings = MainActivity.frameInterface(R.id.menu_item_settings) as FrameSettings

                    if (settings.getWakeUpByBroadcast())
                        DeviceWakeUp.sendWakeUp(DeviceWakeUp.convertToBroadcastAddress(hostIp), port, hostMac)
                    else
                        DeviceWakeUp.sendWakeUp(hostIp, port, hostMac)

                    MainActivity.showMessage("发送成功")
                }

                AlertDialog.Builder(thisView?.context, R.style.DialogTheme)
                        .setTitle("提示:")
                        .setMessage("是否确认唤醒此机器?")
                        .setPositiveButton("确认") { dialogInterface, i -> sendWakeUp() }
                        .setNegativeButton("取消") { dialogInterface, i ->  }
                        .show()
            }
        }

        thisView!!.findViewById<FloatingActionButton>(R.id.addConfigManual).setOnClickListener {
            DialogAddConfig(it.rootView, R.style.DialogTheme).show()
        }

        readConfig()

        return thisView
    }

    fun addConfig(lanDevice: LanDeviceInfo) {
        (listView!!.adapter as LanDeviceInfoAdapter).let {
            if (lanDevice.hostName.isEmpty()) {
                MainActivity.showMessage("配置名称不能为空")

                return
            }
            if (lanDevice.hostIp.isEmpty()) {
                MainActivity.showMessage("IP地址不能为空")

                return
            }
            if (lanDevice.hostMac.isEmpty()) {
                MainActivity.showMessage("MAC地址不能为空")

                return
            }
            if (lanDevice.device.isEmpty()) {
                MainActivity.showMessage("来源不能为空")

                return
            }
            if (lanDevice.port.isEmpty()) {
                MainActivity.showMessage("端口不能为空")

                return
            }

            if (it.isContains(lanDevice)!!)
                MainActivity.showMessage("该配置已存在，请重新尝试")
            else {
                it.addItem(lanDevice)
                MainActivity.showMessage("操作成功")
            }
        }
    }

    fun writeConfig() {
        val editor = thisView!!.context.getSharedPreferences("devices", MODE_PRIVATE).edit()
        val adapter = listView!!.adapter as LanDeviceInfoAdapter

        for (i in 0..(adapter.count - 1))
            editor.putString(i.toString(), adapter.getItem(i).toString())

        editor.apply()
    }

    fun readConfig() {
        val config = thisView!!.context.getSharedPreferences("devices", MODE_PRIVATE)
        val adapter = listView!!.adapter as LanDeviceInfoAdapter

        config.all.forEach {
            val data = (it.value as String).split(",")

            adapter.addItem(LanDeviceInfo(data[0], data[1], data[2], data[3], data[4]))
        }
    }
}