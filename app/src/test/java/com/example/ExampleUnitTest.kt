package com.example

import com.example.data.model.DeviceCategory
import com.example.data.model.ScannedDevice
import com.example.data.vendor.VendorDatabase
import com.example.network.PingResult
import com.example.network.PingTester
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testVendorLookupApple() {
        val vendorInfo = VendorDatabase.lookupVendor("00:03:93:11:22:33")
        assertNotNull(vendorInfo)
        assertEquals("Apple, Inc.", vendorInfo?.vendorName)
        assertEquals(DeviceCategory.SMARTPHONE, vendorInfo?.defaultCategory)
    }

    @Test
    fun testVendorLookupEspressifIot() {
        val vendorInfo = VendorDatabase.lookupVendor("24:0A:C4:AA:BB:CC")
        assertNotNull(vendorInfo)
        assertEquals("Espressif Systems (IoT)", vendorInfo?.vendorName)
        assertEquals(DeviceCategory.IOT, vendorInfo?.defaultCategory)
    }

    @Test
    fun testClassifyGatewayRouter() {
        val (vendor, category) = VendorDatabase.classifyDevice(
            ipAddress = "192.168.1.1",
            gatewayIp = "192.168.1.1",
            macAddress = "00:0A:EB:11:22:33",
            hostname = "router.lan",
            openPorts = listOf(80)
        )
        assertEquals(DeviceCategory.ROUTER, category)
    }

    @Test
    fun testClassifyCctvCameraByPort() {
        val (vendor, category) = VendorDatabase.classifyDevice(
            ipAddress = "192.168.1.55",
            gatewayIp = "192.168.1.1",
            macAddress = "10:12:48:33:44:55", // Hikvision
            hostname = "IPC-HIKVISION",
            openPorts = listOf(554, 80)
        )
        assertEquals(DeviceCategory.CAMERA, category)
    }

    @Test
    fun testClassifyPrinter() {
        val (vendor, category) = VendorDatabase.classifyDevice(
            ipAddress = "192.168.1.80",
            gatewayIp = "192.168.1.1",
            macAddress = "00:00:85:12:34:56", // Canon
            hostname = "Canon-Print",
            openPorts = listOf(9100)
        )
        assertEquals(DeviceCategory.PRINTER, category)
    }

    @Test
    fun testPingStatsCalculation() {
        val results = listOf(
            PingResult(1, "192.168.1.1", 10, true),
            PingResult(2, "192.168.1.1", 20, true),
            PingResult(3, "192.168.1.1", 30, true),
            PingResult(4, "192.168.1.1", -1, false)
        )
        val stats = PingTester.calculateStats(results)
        assertEquals(4, stats.sent)
        assertEquals(3, stats.received)
        assertEquals(10L, stats.minMs)
        assertEquals(30L, stats.maxMs)
        assertEquals(20.0, stats.avgMs, 0.01)
        assertEquals(25, stats.packetLossPercent)
    }

    @Test
    fun testScannedDeviceDisplayName() {
        val deviceWithAlias = ScannedDevice(
            ipAddress = "192.168.1.100",
            customAlias = "TV Ruang Tamu"
        )
        assertEquals("TV Ruang Tamu", deviceWithAlias.displayName)

        val gatewayDevice = ScannedDevice(
            ipAddress = "192.168.1.1",
            isGateway = true
        )
        assertEquals("Router / Gateway", gatewayDevice.displayName)
    }
}
