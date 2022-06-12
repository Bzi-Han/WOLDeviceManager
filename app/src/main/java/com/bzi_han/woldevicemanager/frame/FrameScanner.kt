package com.bzi_han.woldevicemanager.frame

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.wifi.WifiManager
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.bzi_han.woldevicemanager.MainActivity
import com.bzi_han.woldevicemanager.R
import com.bzi_han.woldevicemanager.adapter.LanDeviceInfoAdapter
import com.bzi_han.woldevicemanager.dialog.DialogAddConfig
import com.bzi_han.woldevicemanager.utils.DeviceDiscovery
import kotlinx.android.synthetic.main.activity_frame_scanner.view.*
import java.net.InetAddress

class FrameScanner : Fragment() {
    private var thisView: View? = null
    private var listView: ListView? = null
    private val handler = Handler()

    fun runOnUiThread(execution: () -> Unit) {
        handler.post(execution)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        thisView = inflater.inflate(R.layout.activity_frame_scanner, container, false)
        listView = thisView!!.findViewById(R.id.frame_scanner_listView)
        val newScanButton = thisView!!.findViewById<Button>(R.id.newScan)
        val scanProgress = thisView!!.findViewById<ProgressBar>(R.id.scanProgress)
        val wifiService = thisView!!.context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        when (wifiService.wifiState) {
            1 -> MainActivity.showMessage("若想进行局域网扫描，请先打开WIFI并连接网络然后重新打开APP尝试")
            else -> {
                newScanButton.isEnabled = true

                thisView!!.findViewById<TextView>(R.id.wifiName).text = wifiService.connectionInfo.ssid.replace("\"", "").run { "WIFI名称 : $this" }
                thisView!!.findViewById<TextView>(R.id.wifiMacAddress).text = wifiService.connectionInfo.bssid.run { "WIFI MAC 地址 : $this" }
                thisView!!.findViewById<TextView>(R.id.localMacAddress).text = wifiService.connectionInfo.macAddress.run { "本机 MAC 地址 : $this" }
                thisView!!.findViewById<TextView>(R.id.localIpAddress).text = wifiService.connectionInfo.ipAddress.let {
                    "本机IP地址 : ${it and 0xFF}.${(it shr 8) and 0xFF}.${(it shr 16) and 0xFF}.${(it shr 24) and 0xFF}"
                }
            }
        }

        newScanButton.setOnClickListener {
            if (1 == wifiService.wifiState) {
                MainActivity.showMessage("请打开WIFI并再次尝试")
                return@setOnClickListener
            }
            if (0 == wifiService.connectionInfo.ipAddress) {
                MainActivity.showMessage("请连接网络并再次尝试")
                return@setOnClickListener
            }

            Thread{
                val networkSegmentPrefix = wifiService.connectionInfo.ipAddress.let {
                    "${it and 0xFF}.${(it shr 8) and 0xFF}.${(it shr 16) and 0xFF}."
                }

                if ("0.0.0." == networkSegmentPrefix) {
                    MainActivity.showMessage("错误的本机IP地址")
                    return@Thread
                }

                runOnUiThread { scanProgress.visibility = View.VISIBLE }
                val devices = DeviceDiscovery.scan(networkSegmentPrefix){
                    scanProgress.progress = (it * 100).toInt()
                }

                runOnUiThread{
                    scanProgress.visibility = View.INVISIBLE
                    listView!!.adapter = LanDeviceInfoAdapter(devices, LayoutInflater.from(thisView!!.context)) {
                        DialogAddConfig(it.rootView, R.style.DialogTheme, ((it.parent as View).tag as LanDeviceInfoAdapter.ViewHolder).getData()).show()
                    }
                }
                MainActivity.showMessage("扫描完成")
            }.start()
        }

        return thisView
    }
}