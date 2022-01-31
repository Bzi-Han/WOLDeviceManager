package com.bzi_han.woldevicemanager.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bzi_han.woldevicemanager.MainActivity
import com.bzi_han.woldevicemanager.R
import com.bzi_han.woldevicemanager.dialog.DialogAddConfig
import com.bzi_han.woldevicemanager.entity.LanDeviceInfo
import kotlinx.android.synthetic.main.activity_frame_scanner_listview_item.view.*

class LanDeviceInfoAdapter(data: ArrayList<LanDeviceInfo>, inflater: LayoutInflater, private val runButtonOnclick: (View) -> Unit = {}) : BaseAdapter() {
    private var data: ArrayList<LanDeviceInfo>? = data
    private var inflater: LayoutInflater? = inflater

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val viewHolder: ViewHolder

        val convertView = when (p1) {
            null -> {
                val cv = this.inflater!!.inflate(R.layout.activity_frame_scanner_listview_item, p2, false)
                viewHolder = ViewHolder(cv, runButtonOnclick)
                cv.tag = viewHolder
                cv
            }
            else -> {
                viewHolder = p1.tag as ViewHolder
                p1
            }
        }

        viewHolder.setData(getItem(p0) as LanDeviceInfo)

        return convertView
    }

    override fun getItem(p0: Int): Any {
        return this.data!![p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getCount(): Int {
        return this.data!!.size
    }

    fun addItem(lanDevice: LanDeviceInfo) {
        data?.add(lanDevice)
    }

    fun isContains(lanDevice: LanDeviceInfo): Boolean? {
        return data?.contains(lanDevice)
    }

    class ViewHolder(view: View, runButtonOnclick: (View) -> Unit = {}) {
        var hostName: TextView? = null
        var hostIp: TextView? = null
        var hostMac: TextView? = null
        var device: TextView? = null
        var port: TextView? = null
        private var data: LanDeviceInfo? = null

        init {
            hostName = view.findViewById(R.id.hostName)
            hostIp = view.findViewById(R.id.hostIp)
            hostMac = view.findViewById(R.id.hostMac)
            device = view.findViewById(R.id.device)
            port = view.findViewById(R.id.port)

            view.findViewById<ImageView>(R.id.addConfig).setOnClickListener(runButtonOnclick)
        }

        fun setData(data: LanDeviceInfo) {
            hostName?.text = data.hostName
            hostIp?.text = data.hostIp.run { "IP地址 : ${this}" }
            hostMac?.text = data.hostMac.run { "MAC地址 : ${this}" }
            device?.text = data.device.run { "来源 : ${this}" }
            port?.text = data.port.run { "端口 : ${this}" }
            this.data = data
        }

        fun getData(): LanDeviceInfo? {
            return this.data
        }
    }

}