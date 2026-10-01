package com.example.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

data class PingResult(
    val sequence: Int,
    val ip: String,
    val timeMs: Long,
    val isSuccess: Boolean
)

data class PingStats(
    val sent: Int = 0,
    val received: Int = 0,
    val minMs: Long = 0,
    val maxMs: Long = 0,
    val avgMs: Double = 0.0,
    val packetLossPercent: Int = 0
)

object PingTester {

    /**
     * Executes single ping measurement with dual-probe (socket fallback for firewall resilience).
     */
    suspend fun ping(ip: String, timeoutMs: Int = 1000, sequence: Int = 1): PingResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        var success = false

        // 1. Try InetAddress isReachable
        try {
            val address = InetAddress.getByName(ip)
            success = address.isReachable(timeoutMs)
        } catch (_: Exception) {}

        // 2. If isReachable fails (e.g. non-root ICMP restriction or ICMP blocked), try quick socket probe
        if (!success) {
            val testPorts = intArrayOf(80, 443, 22, 53, 445, 8080)
            for (p in testPorts) {
                try {
                    Socket().use { s ->
                        s.connect(InetSocketAddress(ip, p), timeoutMs.coerceAtMost(300))
                        success = true
                    }
                    if (success) break
                } catch (_: Exception) {}
            }
        }

        val elapsed = (System.currentTimeMillis() - start).coerceAtLeast(1)

        PingResult(
            sequence = sequence,
            ip = ip,
            timeMs = if (success) elapsed else -1,
            isSuccess = success
        )
    }

    fun calculateStats(results: List<PingResult>): PingStats {
        if (results.isEmpty()) return PingStats()
        val sent = results.size
        val successful = results.filter { it.isSuccess && it.timeMs >= 0 }
        val received = successful.size
        val loss = if (sent > 0) ((sent - received) * 100) / sent else 0

        if (successful.isEmpty()) {
            return PingStats(sent = sent, received = 0, packetLossPercent = 100)
        }

        val times = successful.map { it.timeMs }
        val min = times.minOrNull() ?: 0
        val max = times.maxOrNull() ?: 0
        val avg = times.average()

        return PingStats(
            sent = sent,
            received = received,
            minMs = min,
            maxMs = max,
            avgMs = avg,
            packetLossPercent = loss
        )
    }
}
