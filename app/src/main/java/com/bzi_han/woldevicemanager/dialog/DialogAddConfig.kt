package com.bzi_han.woldevicemanager.dialog

import android.app.Dialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.bzi_han.woldevicemanager.MainActivity
import com.bzi_han.woldevicemanager.R
import com.bzi_han.woldevicemanager.entity.LanDeviceInfo

class DialogAddConfig(parentView: View, dialogStyle: Int, lanDevice: LanDeviceInfo? = null) : Dialog(parentView.context, dialogStyle) {
    private var thisView: View? = null
    private var hostName: EditText? = null
    private var hostIp: EditText? = null
    private var hostMac: EditText? = null
    private var device: EditText? = null
    private var port: EditText? = null

    init {
        thisView = LayoutInflater.from(context).inflate(R.layout.activity_dialog_addconfig, parentView as ViewGroup, false)

        setContentView(thisView)
        hostName = thisView?.findViewById(R.id.hostName)
        hostIp = thisView?.findViewById(R.id.hostIp)
        hostMac = thisView?.findViewById(R.id.hostMac)
        device = thisView?.findViewById(R.id.device)
        port = thisView?.findViewById(R.id.port)

        lanDevice?.let {
            hostName?.setText(lanDevice.hostName)
            hostIp?.setText(lanDevice.hostIp)
            hostMac?.setText(lanDevice.hostMac)
            device?.setText(lanDevice.device)
            port?.setText(lanDevice.port)
        }

        thisView?.findViewById<Button>(R.id.addDialogSave)?.setOnClickListener {
            val mac = hostMac?.text.toString().replace("-", ":")

            if (6 != mac.split(":").size) {
                MainActivity.showMessage("MAC地址错误，请重试")

                return@setOnClickListener
            }

            MainActivity.addLocalConfig(LanDeviceInfo(
                    hostName?.text.toString(),
                    hostIp?.text.toString(),
                    mac,
                    device?.text.toString(),
                    port?.text.toString()
            ))
        }
    }
}