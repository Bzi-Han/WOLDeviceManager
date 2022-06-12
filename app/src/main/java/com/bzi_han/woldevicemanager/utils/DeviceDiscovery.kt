package com.bzi_han.woldevicemanager.utils

import android.util.Log
import com.bzi_han.woldevicemanager.entity.LanDeviceInfo
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.lang.RuntimeException
import java.lang.StringBuilder
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.nio.ByteBuffer

import java.util.ArrayList

object DeviceDiscovery {
    private val sender = DatagramSocket()

    /**
     * @param networkSegmentPrefix 要扫描的目标网段前缀 192.168.1.
     */
    fun scan(networkSegmentPrefix: String, progress: (Float) -> Unit = {}): ArrayList<LanDeviceInfo> {
        val result = ArrayList<LanDeviceInfo>()
        val data = ByteArray(1)

        progress(0f)
        for (i in 1..254) {
            kotlin.runCatching { sender.send(DatagramPacket(data, 0, 1, InetAddress.getByName(networkSegmentPrefix + i), 888)) }
            progress(i / 254f)
        }
        progress(1f)

        progress(0f)
        val table = runCommand("cat", "/proc/net/arp")!!
                .replace("\r", "")
                .split("\n")
        for ((i, row) in table.withIndex()) {
            val col = row.let {
                val results = ArrayList<String>()

                var flag = true
                val sb = StringBuilder()
                for (c in it.trim()) {
                    when (c) {
                        ' ' -> {
                            if (flag) {
                                results.add(sb.toString())
                                sb.clear()
                                flag = false
                            }
                        }
                        else -> {
                            sb.append(c)
                            flag = true
                        }
                    }
                }
                if (flag)
                    results.add(sb.toString())

                results
            }

            when (col.size) {
                6 -> {
                    if ("00:00:00:00:00:00" != col[3] && col[0].contains(networkSegmentPrefix)) {
                        result.add(LanDeviceInfo(
                                getComputerName(col[0]),
                                col[0],
                                col[3],
                                col[5],
                                "888"
                        ))
                    }
                }
            }

            progress(i / table.size.toFloat())
        }
        progress(1f)

        return result
    }

    private fun runCommand(vararg cmd: String): String? = runCommand(listOf(*cmd))
    private fun runCommand(
            cmd: List<String>,
            workingDir: File = File(".")
    ): String? = runCatching {
        ProcessBuilder(cmd)
                .directory(workingDir)
                .redirectErrorStream(true)
                .start()
                .inputStream.bufferedReader().readText()
    }.onFailure { it.printStackTrace() }.getOrNull()

    private fun getComputerName(targetIp: String): String {
        var domainNameOffset: Int

        // 组包
        val packet = targetIp.let {
            val ips = it.split(".")
            val hard01 = byteArrayOf(0x01, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00)
            val hard02 = byteArrayOf(0x00, 0x0c, 0x00, 0x01)
            val packetBuilder = ByteBuffer.allocate(256)

            packetBuilder.putShort(888) //标识符
            packetBuilder.put(hard01) //硬编码01

            // 编码查询地址
            domainNameOffset = packetBuilder.position()
            for (i in 3 downTo 0) {
                packetBuilder.put(ips[i].length.toByte())
                packetBuilder.put(ips[i].toByteArray())
            }
            packetBuilder.put(7)
            packetBuilder.put("in-addr".toByteArray())
            packetBuilder.put(4)
            packetBuilder.put("arpa".toByteArray())
            packetBuilder.put(0)

            packetBuilder.put(hard02) //硬编码02
            domainNameOffset = packetBuilder.position() - domainNameOffset + 6 + packetBuilder.position()

            DatagramPacket(packetBuilder.array(), 0, packetBuilder.position(), InetAddress.getByName("224.0.0.252"), 5355)
        }

        // 发送请求
        kotlin.runCatching { sender.send(packet) }.onFailure { return "UnReachableDevice" }

        // 接收返回
        val recvBuffer = ByteBuffer.allocate(1024)
        val recvPacket = DatagramPacket(recvBuffer.array(), recvBuffer.capacity())
        sender.soTimeout = 1000
        kotlin.runCatching {
            sender.receive(recvPacket)
        }.onFailure { return "UnKnowDevice" }

        // 读取域名
        recvBuffer.position(domainNameOffset)
        val domainNameByteArray = ByteBuffer.allocate(recvBuffer.get().toInt())
        recvBuffer.get(domainNameByteArray.array())

        return String(domainNameByteArray.array())
    }
}
