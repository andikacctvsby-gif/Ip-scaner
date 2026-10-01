package com.example.network

import com.example.data.model.OpenPortInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlinx.coroutines.withContext
import java.net.InetSocketAddress
import java.net.Socket

object PortScanner {

    data class PortDefinition(
        val port: Int,
        val service: String,
        val description: String
    )

    val COMMON_PORTS = listOf(
        PortDefinition(21, "FTP", "Protokol Transfer Berkas (File Transfer)"),
        PortDefinition(22, "SSH", "Secure Shell (Remote Akses Aman)"),
        PortDefinition(23, "Telnet", "Akses Baris Perintah Tidak Terenkripsi"),
        PortDefinition(25, "SMTP", "Protokol Pengiriman Email"),
        PortDefinition(53, "DNS", "Layanan Domain Name System"),
        PortDefinition(80, "HTTP", "Layanan Web Standar"),
        PortDefinition(110, "POP3", "Pengambilan Email"),
        PortDefinition(139, "NetBIOS", "Layanan File Sharing Windows"),
        PortDefinition(143, "IMAP", "Protokol Email Interaktif"),
        PortDefinition(443, "HTTPS", "Layanan Web Terenkripsi SSL/TLS"),
        PortDefinition(445, "SMB", "Berbagi Berkas Jaringan Microsoft / Samba"),
        PortDefinition(554, "RTSP", "Streaming Video Real Time (Kamera CCTV)"),
        PortDefinition(631, "IPP", "Internet Printing Protocol (Printer Jaringan)"),
        PortDefinition(8000, "HTTP-ALT", "Server Web Alternatif / Dev"),
        PortDefinition(8008, "DIAL/Cast", "Google Cast / Smart TV Discovery"),
        PortDefinition(8009, "Chromecast", "Protokol Google Chromecast Audio/Video"),
        PortDefinition(8080, "HTTP-Proxy", "Proxy Web & Panel Admin Alternatif"),
        PortDefinition(8443, "HTTPS-Alt", "Web Terenkripsi Alternatif"),
        PortDefinition(8888, "HTTP-Dev", "Web Dashboard / MikroTik Web"),
        PortDefinition(9100, "RAW-Print", "Port Pencetakan Langsung Printer"),
        PortDefinition(3306, "MySQL", "Basis Data MySQL / MariaDB"),
        PortDefinition(3389, "RDP", "Remote Desktop Windows"),
        PortDefinition(5000, "Synology-HTTP", "Manajemen Storage Synology NAS"),
        PortDefinition(5001, "Synology-HTTPS", "Manajemen Aman Synology NAS"),
        PortDefinition(62078, "Apple-Sync", "Protokol Sinkronisasi Apple iOS / macOS")
    )

    private val portDefinitionMap = COMMON_PORTS.associateBy { it.port }

    /**
     * Checks if a single port is open on target host.
     */
    suspend fun checkPort(ip: String, port: Int, timeoutMs: Int = 300): OpenPortInfo = withContext(Dispatchers.IO) {
        val def = portDefinitionMap[port]
        val serviceName = def?.service ?: "Port $port"
        val desc = def?.description ?: "Layanan Khusus"

        var isOpen = false
        try {
            Socket().use { socket ->
                socket.connect(InetSocketAddress(ip, port), timeoutMs)
                isOpen = true
            }
        } catch (_: Exception) {
            isOpen = false
        }

        OpenPortInfo(
            port = port,
            serviceName = serviceName,
            description = desc,
            isOpen = isOpen
        )
    }

    /**
     * Scans standard top ports on target IP concurrently.
     */
    suspend fun scanCommonPorts(
        ip: String,
        timeoutMs: Int = 350,
        onPortChecked: ((OpenPortInfo) -> Unit)? = null
    ): List<OpenPortInfo> = coroutineScope {
        val semaphore = Semaphore(15)
        val deferred = COMMON_PORTS.map { def ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val result = checkPort(ip, def.port, timeoutMs)
                    if (result.isOpen) {
                        onPortChecked?.invoke(result)
                    }
                    result
                }
            }
        }
        deferred.awaitAll().filter { it.isOpen }
    }

    /**
     * Scans custom port range (e.g., startPort to endPort) on target IP.
     */
    suspend fun scanCustomRange(
        ip: String,
        startPort: Int,
        endPort: Int,
        timeoutMs: Int = 250,
        onProgress: ((currentPort: Int, openFound: OpenPortInfo?) -> Unit)? = null
    ): List<OpenPortInfo> = coroutineScope {
        val semaphore = Semaphore(25)
        val validRange = (startPort.coerceAtLeast(1)..endPort.coerceAtMost(65535)).toList()
        val openPorts = mutableListOf<OpenPortInfo>()

        validRange.map { port ->
            async(Dispatchers.IO) {
                semaphore.withPermit {
                    val result = checkPort(ip, port, timeoutMs)
                    if (result.isOpen) {
                        synchronized(openPorts) { openPorts.add(result) }
                    }
                    onProgress?.invoke(port, if (result.isOpen) result else null)
                    result
                }
            }
        }.awaitAll()

        openPorts.sortedBy { it.port }
    }
}
