package com.bzi_han.woldevicemanager

import android.net.DnsResolver
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.BottomNavigationView
import android.support.v4.app.Fragment
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.bzi_han.woldevicemanager.entity.LanDeviceInfo
import com.bzi_han.woldevicemanager.frame.FrameConfig
import com.bzi_han.woldevicemanager.frame.FrameScanner
import com.bzi_han.woldevicemanager.frame.FrameSettings
import com.bzi_han.woldevicemanager.utils.DeviceDiscovery
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.nio.ByteBuffer

class MainActivity : AppCompatActivity() {
    private val frameMap = mapOf(
            Pair(R.id.menu_item_scanner, FrameScanner()),
            Pair(R.id.menu_item_config, FrameConfig()),
            Pair(R.id.menu_item_settings, FrameSettings())
    )
    private var lastShow = R.id.menu_item_scanner
    private var exitState = false

    companion object {
        private var activity: MainActivity? = null

        fun showMessage(message: String) {
            activity!!.runOnUiThread {
                Toast.makeText(activity!!.applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }

        fun updateFrame(id: Int) {
            activity!!.switchFrame(id)
        }

        fun addLocalConfig(lanDevice: LanDeviceInfo) {
            (activity!!.frameMap[R.id.menu_item_config] as FrameConfig).addConfig(lanDevice)
            if (activity!!.lastShow == R.id.menu_item_config)
                updateFrame(R.id.menu_item_config)
        }

        fun frameInterface(id: Int):Fragment? {
            return activity!!.frameMap[id]
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activity = this

        findViewById<BottomNavigationView>(R.id.menu_bottomBar).setOnNavigationItemSelectedListener { switchFrame(it.itemId) }

        val ft = supportFragmentManager.beginTransaction()
        frameMap.forEach {
            ft.add(R.id.frame_layout, it.value)
            ft.hide(it.value)
        }
        ft.commit()
        switchFrame(lastShow)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (4 != keyCode)
            return super.onKeyDown(keyCode, event)

        if (!exitState) {
            showMessage("再按一次退出")
            exitState = true
            Handler().postDelayed({ exitState = false }, 800)
        } else {
            (frameMap[R.id.menu_item_config] as FrameConfig).writeConfig()
            finish()
        }

        return true
    }

    private fun switchFrame(id: Int): Boolean {
        val ft = supportFragmentManager.beginTransaction()
        val frame = frameMap[id]

        frame?.let {
            ft.hide(frameMap[lastShow])
            ft.show(frame)
            ft.commitAllowingStateLoss()
            lastShow = id
        }

        return true
    }
}
