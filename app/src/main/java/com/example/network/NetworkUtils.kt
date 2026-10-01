package com.example.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileReader
import java.net.*
import java.util.*

data class WifiNetworkDetails(
    val isConnected: Boolean = false,
    val ssid: String = "Tidak Terhubung",
    val bssid: String = "-",
    val ipAddress: String = "0.0.0.0",
    val gatewayIp: String = "0.0.0.0",
    val subnetMask: String = "255.255.255.0",
    val prefixLength: Int = 24,
    val signalStrengthDbm: Int = 0,
    val linkSpeedMbps: Int = 0,
    val frequencyMhz: Int = 0,
    val publicIp: String? = null
)

object NetworkUtils {

    /**
     * Retrieves full current Wi-Fi and IP network details.
     */
    fun getNetworkDetails(context: Context): WifiNetworkDetails {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true

        if (!isWifi) {
            // Check for Ethernet or active local interface
            val localIp = getLocalIpAddress()
            if (localIp != null) {
                return WifiNetworkDetails(
                    isConnected = true,
                    ssid = "Jaringan Lokal (LAN/Ethernet)",
                    ipAddress = localIp,
                    gatewayIp = getGatewayFromIp(localIp),
                    subnetMask = "255.255.255.0",
                    prefixLength = 24
                )
            }
            return WifiNetworkDetails(isConnected = false)
        }

        val wifiInfo = wifiManager.connectionInfo
        val dhcpInfo = wifiManager.dhcpInfo

        var ssid = wifiInfo?.ssid?.replace("\"", "") ?: "WiFi Terhubung"
        if (ssid == "<unknown ssid>" || ssid.isEmpty()) {
            ssid = "WiFi Terhubung"
        }

        val bssid = wifiInfo?.bssid ?: "-"
        val rssi = wifiInfo?.rssi ?: 0
        val linkSpeed = wifiInfo?.linkSpeed ?: 0
        val freq = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) wifiInfo?.frequency ?: 0 else 0

        val localIp = getLocalIpAddress() ?: formatIpAddress(dhcpInfo?.ipAddress ?: 0)
        val gateway = formatIpAddress(dhcpInfo?.gateway ?: 0).takeIf { it != "0.0.0.0" } ?: getGatewayFromIp(localIp)
        val subnet = formatIpAddress(dhcpInfo?.netmask ?: 0).takeIf { it != "0.0.0.0" } ?: "255.255.255.0"
        val prefix = calculatePrefixLength(subnet)

        return WifiNetworkDetails(
            isConnected = true,
            ssid = ssid,
            bssid = bssid,
            ipAddress = localIp,
            gatewayIp = gateway,
            subnetMask = subnet,
            prefixLength = prefix,
            signalStrengthDbm = rssi,
            linkSpeedMbps = linkSpeed,
            frequencyMhz = freq
        )
    }

    private fun formatIpAddress(ip: Int): String {
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    private fun getGatewayFromIp(ip: String): String {
        val parts = ip.split(".")
        return if (parts.size == 4) "${parts[0]}.${parts[1]}.${parts[2]}.1" else "192.168.1.1"
    }

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (intf.isLoopback || !intf.isUp) continue
                val addrs = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress
                    }
                }
            }
        } catch (_: Exception) {}
        return null
    }

    private fun calculatePrefixLength(netmask: String): Int {
        try {
            val parts = netmask.split(".")
            if (parts.size != 4) return 24
            var count = 0
            for (part in parts) {
                var byteVal = part.toInt()
                while (byteVal > 0) {
                    if (byteVal and 1 == 1) count++
                    byteVal = byteVal shr 1
                }
            }
            return if (count in 8..30) count else 24
        } catch (_: Exception) {
            return 24
        }
    }

    /**
     * Reads system ARP table to find MAC address associated with IP.
     */
    fun getMacFromArp(ip: String): String? {
        try {
            val reader = BufferedReader(FileReader("/proc/net/arp"))
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val tokens = line?.split("\\s+".toRegex()) ?: continue
                if (tokens.size >= 4 && tokens[0] == ip) {
                    val mac = tokens[3]
                    if (mac != "00:00:00:00:00:00" && mac.matches("([0-9a-fA-F]{2}:){5}[0-9a-fA-F]{2}".toRegex())) {
                        reader.close()
                        return mac.uppercase()
                    }
                }
            }
            reader.close()
        } catch (_: Exception) {}
        return null
    }

    /**
     * Fetches public IP asynchronously.
     */
    suspend fun fetchPublicIp(): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.ipify.org")
            val conn = url.openConnection() as HttpURLConnection
            conn.connectTimeout = 3000
            conn.readTimeout = 3000
            conn.requestMethod = "GET"
            if (conn.responseCode == 200) {
                return@withContext conn.inputStream.bufferedReader().readText().trim()
            }
        } catch (_: Exception) {}
        null
    }

    /**
     * Sends Wake-on-LAN magic packet to specified MAC address.
     */
    suspend fun sendWakeOnLan(macAddress: String, broadcastIp: String = "255.255.255.255"): Boolean = withContext(Dispatchers.IO) {
        try {
            val cleanMac = macAddress.replace(":", "").replace("-", "")
            if (cleanMac.length != 12) return@withContext false

            val macBytes = ByteArray(6)
            for (i in 0 until 6) {
                macBytes[i] = cleanMac.substring(i * 2, i * 2 + 2).toInt(16).toByte()
            }

            val packetBytes = ByteArray(6 + 16 * macBytes.size)
            // 6 bytes of 0xFF
            for (i in 0 until 6) {
                packetBytes[i] = 0xFF.toByte()
            }
            // 16 repetitions of MAC address
            var index = 6
            for (i in 0 until 16) {
                System.arraycopy(macBytes, 0, packetBytes, index, macBytes.size)
                index += macBytes.size
            }

            val address = InetAddress.getByName(broadcastIp)
            val packet = DatagramPacket(packetBytes, packetBytes.size, address, 9)
            val socket = DatagramSocket()
            socket.broadcast = true
            socket.send(packet)
            socket.close()
            true
        } catch (_: Exception) {
            false
        }
    }
}
