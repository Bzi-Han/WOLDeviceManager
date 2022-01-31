package com.bzi_han.woldevicemanager.frame

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.ListView
import android.widget.Switch
import com.bzi_han.woldevicemanager.MainActivity
import com.bzi_han.woldevicemanager.R

class FrameSettings : Fragment() {
    private var thisView: View? = null
    private val handler = Handler()
    private var configOperator: SharedPreferences? = null
    private var wakeUpByBroadcastSwitch: Switch? = null

    fun runOnUiThread(execution: () -> Unit) {
        handler.post(execution)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        thisView = inflater.inflate(R.layout.activity_frame_settings, container, false)

        configOperator = thisView!!.context.getSharedPreferences("settings", MODE_PRIVATE)

        wakeUpByBroadcastSwitch = thisView!!.findViewById(R.id.wakeUpByBroadcast)
        wakeUpByBroadcastSwitch?.isChecked = configOperator!!.getBoolean("wakeUpByBroadcast", true)
        wakeUpByBroadcastSwitch?.setOnCheckedChangeListener { compoundButton, b -> configOperator!!.edit().putBoolean("wakeUpByBroadcast", b).apply() }

        return thisView
    }

    fun getWakeUpByBroadcast(): Boolean {
        return configOperator!!.getBoolean("wakeUpByBroadcast", true)
    }
}