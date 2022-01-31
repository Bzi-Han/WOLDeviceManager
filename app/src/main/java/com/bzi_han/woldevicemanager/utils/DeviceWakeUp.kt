package com.bzi_han.woldevicemanager.utils

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

object DeviceWakeUp {
    private val sender = DatagramSocket()

    fun convertToBroadcastAddress(ip: String): String {
        return ip.split(".").run {
            "${this[0]}.${this[1]}.${this[2]}.255"
        }
    }

    fun sendWakeUp(host: String, port: String, mac: String) {
        val macProcess = mac.toUpperCase().split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val prefix = byteArrayOf(0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte(), 0xFF.toByte())
        val suffix = ByteArray(6)
        val data = ByteArray(prefix.size + suffix.size * 16)

        for (i in macProcess.indices) {
            suffix[i] = Integer.parseInt(macProcess[i], 16).toByte()
        }
        System.arraycopy(prefix, 0, data, 0, prefix.size)
        for (i in 0..15) {
            System.arraycopy(suffix, 0, data, prefix.size + suffix.size * i, suffix.size)
        }

        val packet = DatagramPacket(data, data.size, InetAddress.getByName(host), Integer.parseInt(port))

        Thread {
            sender.send(packet)
        }.start()
    }
}