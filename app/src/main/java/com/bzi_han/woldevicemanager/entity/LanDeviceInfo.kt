package com.bzi_han.woldevicemanager.entity

class LanDeviceInfo(val hostName: String, val hostIp: String, val hostMac: String, val device: String = "", val port: String = "") {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as LanDeviceInfo

        if (hostName != other.hostName) return false
        if (hostIp != other.hostIp) return false
        if (hostMac != other.hostMac) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hostName.hashCode()
        result = 31 * result + hostIp.hashCode()
        result = 31 * result + hostMac.hashCode()
        return result
    }

    override fun toString(): String {
        return "$hostName,$hostIp,$hostMac,$device,$port"
    }
}
