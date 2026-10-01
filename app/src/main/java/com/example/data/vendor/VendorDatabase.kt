package com.example.data.vendor

import com.example.data.model.DeviceCategory

object VendorDatabase {

    data class VendorInfo(
        val vendorName: String,
        val defaultCategory: DeviceCategory
    )

    // Prefix map (normalized uppercase hex, first 6 characters, e.g. "001A2B")
    private val prefixMap = HashMap<String, VendorInfo>(400)

    init {
        // Apple
        registerPrefixes(
            listOf(
                "000393", "000502", "000A27", "0010FA", "0017F2", "001CB3", "001E52", "002312",
                "002500", "002608", "0026B0", "040CCE", "041552", "042665", "045453", "086698",
                "14109F", "186590", "20EE28", "28CFE9", "3C0754", "3C15C2", "406C8F", "444C0C",
                "48605F", "50EAD6", "600308", "705681", "748D08", "784F43", "80BE05", "843835",
                "8866A5", "8C8590", "90FD61", "A483E7", "AC87A3", "B03495", "B8098A", "C869CD",
                "D0034B", "D4619D", "DCA904", "E0B9BA", "F4F15A", "F81EDF", "FCFC48", "F01898",
                "F437B7", "F4F951", "A85C2C", "B8782E", "68DBCA", "ACDE48", "F0DBF8"
            ),
            VendorInfo("Apple, Inc.", DeviceCategory.SMARTPHONE)
        )

        // Samsung Electronics
        registerPrefixes(
            listOf(
                "0007AB", "001247", "001632", "0017C9", "0021D1", "002490", "002637", "0418D6",
                "08373D", "08D42B", "103047", "1867B0", "244B03", "30CDA7", "342387", "44F459",
                "4CBC98", "5001D9", "58C38B", "64B310", "68EBAE", "784B87", "8425DB", "90F1AA",
                "A0821F", "A47733", "B407F9", "BC8CCD", "C4731E", "D059E4", "E47CF9", "F072EA",
                "F47B5E", "5C0A5B", "3C8375", "1C5CF2", "E8039A", "2CF05D"
            ),
            VendorInfo("Samsung Electronics", DeviceCategory.SMARTPHONE)
        )

        // Xiaomi Communications
        registerPrefixes(
            listOf(
                "00EC0A", "14F65A", "185936", "286C07", "34CE00", "38A4ED", "50642B", "584498",
                "640980", "742344", "7802F8", "88C397", "8CBEBE", "A444D1", "B0E5ED", "C40BCB",
                "D4970B", "E446DA", "FC643A", "04CF4B", "5448E8", "80AD16", "9C99A0", "B0C554"
            ),
            VendorInfo("Xiaomi Communications", DeviceCategory.SMARTPHONE)
        )

        // Google
        registerPrefixes(
            listOf(
                "001A11", "3C5AB4", "546009", "94EBCD", "D86C63", "F4F5DB", "F88FCA",
                "A47733", "48D705", "D4E5D4"
            ),
            VendorInfo("Google LLC", DeviceCategory.SMARTPHONE)
        )

        // Espressif Systems (ESP8266 & ESP32 IoT chips)
        registerPrefixes(
            listOf(
                "18FE34", "240AC4", "2462AB", "246F28", "24B2DE", "2CF432", "30AEA4", "3C71BF",
                "483FDA", "545AA6", "5CCF7F", "600194", "68C63A", "70039F", "7C87CE", "840D8E",
                "84F3EB", "9097D5", "94B97E", "A4CF12", "A848FA", "AC67B2", "B4E62D", "BCDDC2",
                "C44F33", "CC50E3", "D8A01D", "DC4F22", "E09806", "E868E7", "ECFABC", "F4CFA2"
            ),
            VendorInfo("Espressif Systems (IoT)", DeviceCategory.IOT)
        )

        // Tuya Smart (IoT / Smart Home)
        registerPrefixes(
            listOf(
                "0054AF", "102C6B", "20F41B", "2CAA8E", "381F8D", "408E2C", "441793", "508A06",
                "68572D", "70C94E", "7CF666", "84F703", "A09208", "C049EF", "D0C5D3", "E89A8F"
            ),
            VendorInfo("Tuya Smart Home", DeviceCategory.IOT)
        )

        // Raspberry Pi Foundation
        registerPrefixes(
            listOf("B827EB", "DCA632", "E45F01", "28CDC1"),
            VendorInfo("Raspberry Pi Foundation", DeviceCategory.IOT)
        )

        // TP-Link
        registerPrefixes(
            listOf(
                "000AEB", "001478", "0019E0", "001D0F", "002127", "0023CD", "002586", "002719",
                "14CC20", "1C3BF3", "30B5C2", "30DE4B", "50C7BF", "60E327", "6466B3", "704F57",
                "7405A5", "784476", "8416F9", "90F652", "A0F3C1", "B04E26", "B09575", "C04A00",
                "C46E1F", "D80D17", "D84732", "EC086B", "F4EC38", "F81A67", "54AF97"
            ),
            VendorInfo("TP-Link Technologies", DeviceCategory.ROUTER)
        )

        // D-Link
        registerPrefixes(
            listOf(
                "00055D", "000D88", "001195", "001346", "0015E9", "00179A", "00195B", "001B11",
                "001E58", "002191", "0022B0", "002401", "00265A", "14D64D", "1C7EE5", "28107B",
                "340804", "78542E", "9094E4", "B8A386", "C8D3A3", "CCB255", "E063DA", "F07D68"
            ),
            VendorInfo("D-Link Corporation", DeviceCategory.ROUTER)
        )

        // Netgear
        registerPrefixes(
            listOf(
                "00095B", "000FB5", "00146C", "00184D", "001B2F", "001E2A", "001F33", "00223F",
                "0024B2", "0026F2", "04A151", "0836C9", "100C6B", "10DA43", "20E52A", "288088",
                "2CB05D", "30469A", "4494FC", "6CB0CE", "841B5E", "9C3DCF", "A00460", "A42BB0",
                "B07FB9", "C03F0E", "C40415", "C43DC7", "E0469A", "E4F4C6"
            ),
            VendorInfo("Netgear", DeviceCategory.ROUTER)
        )

        // MikroTik
        registerPrefixes(
            listOf(
                "000C42", "085531", "18FD74", "2CC81B", "488F5A", "4C5E0C", "64D154", "6C3B6B",
                "744D28", "789A18", "B869F4", "C4AD34", "CC2DE0", "D401C3", "D4CA6D", "E48D8C"
            ),
            VendorInfo("MikroTik", DeviceCategory.ROUTER)
        )

        // Ubiquiti Networks
        registerPrefixes(
            listOf(
                "00156D", "002722", "0418D6", "24A43C", "44D9E7", "68D79A", "7483C2", "788A20",
                "802AA8", "B4FBE4", "DC9FDB", "E063DA", "F09FC2"
            ),
            VendorInfo("Ubiquiti Networks", DeviceCategory.ROUTER)
        )

        // Huawei
        registerPrefixes(
            listOf(
                "001882", "001E10", "002568", "00259E", "00464B", "00E0FC", "04021F", "0425C5",
                "047970", "04C06F", "0819A6", "087A4C", "0C96BF", "101B54", "104780", "10C61F",
                "14B968", "2008ED", "2469A5", "283152", "285FDB", "3400A3", "3847BC", "3CDFB1",
                "4846FB", "548998", "5C7D5E", "60DE44", "707BE8", "786A89", "80B686", "888603",
                "904E91", "A49947", "AC853D", "B41513", "BC25E0", "C4072F", "CC96A0", "D46AA8"
            ),
            VendorInfo("Huawei Technologies", DeviceCategory.ROUTER)
        )

        // ZTE Corporation
        registerPrefixes(
            listOf(
                "0019C6", "001E73", "002293", "0026ED", "08181A", "0C130B", "146080", "200CC8",
                "286ED4", "34E0CF", "3897D6", "48282F", "5065F3", "5422F8", "681AB2", "6CACB9",
                "74B57E", "84742A", "88E3AB", "981333", "A0ECF9", "B075D5", "C87B5B", "CC7B35"
            ),
            VendorInfo("ZTE Corporation", DeviceCategory.ROUTER)
        )

        // Intel Corporation
        registerPrefixes(
            listOf(
                "0002B3", "000347", "000423", "000CF1", "000E0C", "001302", "0013E8", "001500",
                "001676", "0019D1", "001B21", "001CBF", "001E64", "00215C", "0022FA", "002314",
                "0024D7", "0026C6", "00270E", "002710", "081196", "087190", "0C8BFD", "144F8A",
                "185E0F", "1C697A", "28704E", "340286", "3CF862", "4851B7", "4C79BA", "548CA0",
                "606C66", "6805CA", "701CE7", "782BCB", "8086F2", "84FDD1", "88B111", "8C8D28",
                "984FEE", "A44CC8", "AC7289", "B49691", "C89402", "DC5360", "E4A471", "F8633F"
            ),
            VendorInfo("Intel (PC / Laptop)", DeviceCategory.COMPUTER)
        )

        // Dell
        registerPrefixes(
            listOf(
                "00065B", "000874", "000BDB", "001143", "001372", "001422", "0016F0", "00188B",
                "0019B9", "001AA0", "001C23", "001D09", "001E4F", "002170", "002219", "0024E8",
                "0026B9", "14FEB5", "180373", "1866DA", "24B6FD", "3417EB", "44A842", "549F35",
                "74867A", "842B2B", "989096", "A4BADB", "B44506", "B8AC6F", "C8F750", "D4BED9"
            ),
            VendorInfo("Dell Inc.", DeviceCategory.COMPUTER)
        )

        // HP / Hewlett-Packard
        registerPrefixes(
            listOf(
                "0001E6", "000802", "000BCD", "000E7F", "00110A", "001438", "001635", "0017A4",
                "0018FE", "001A4B", "001CC4", "001E0B", "00215A", "00237D", "0025B3", "002655",
                "101F74", "18A958", "2C4138", "308D99", "3CD92B", "40A8F0", "480FCF", "5CB901",
                "705A0F", "80C16E", "9457A5", "A0D3C1", "B499BA", "C8CBB8", "D067E5", "E4115B"
            ),
            VendorInfo("HP Inc.", DeviceCategory.COMPUTER)
        )

        // Lenovo
        registerPrefixes(
            listOf(
                "0016D3", "001EEC", "002186", "04766E", "083E8E", "141333", "207693", "281878",
                "3C970E", "482CA0", "503EAA", "54E1AD", "60D819", "6CC7EC", "70720D", "74B30A",
                "802BF9", "8C1645", "94659C", "A41731", "B0359F", "C85B76", "DC85DE", "E0D55E"
            ),
            VendorInfo("Lenovo", DeviceCategory.COMPUTER)
        )

        // Sony
        registerPrefixes(
            listOf(
                "00014A", "00041F", "000E07", "001315", "0015C1", "0019C5", "001DBA", "00248D",
                "080046", "0CFE45", "1048B1", "280DFC", "307512", "544249", "709E29", "78843C",
                "880355", "9003B7", "A8E3EE", "BC60A7", "CC9E00", "D8D43C", "E063E5", "F8461C"
            ),
            VendorInfo("Sony Corporation", DeviceCategory.SMART_TV)
        )

        // LG Electronics
        registerPrefixes(
            listOf(
                "0005C9", "0019A1", "001C62", "001E75", "0021FB", "002483", "0026E2", "10683F",
                "14C913", "203D66", "344DF7", "40B034", "58A2B5", "64995D", "702C1F", "74A722",
                "88C9D0", "98D6F7", "A823FE", "B4EED4", "C4366C", "CC2D8C", "E85B5B"
            ),
            VendorInfo("LG Electronics", DeviceCategory.SMART_TV)
        )

        // Roku
        registerPrefixes(
            listOf(
                "000D4B", "080581", "20F543", "24A52C", "404E36", "586B14", "84EA99", "B0A737",
                "C83A35", "D4933F"
            ),
            VendorInfo("Roku, Inc.", DeviceCategory.SMART_TV)
        )

        // Amazon
        registerPrefixes(
            listOf(
                "00FC8B", "0C47C9", "18742E", "244CE3", "34D270", "380146", "40B4CD", "440049",
                "44650D", "50F5DA", "6837E9", "6854FD", "74C246", "78E103", "84D6D0", "949F3E",
                "AC63BE", "B47C9C", "CCF411", "F08173", "F0D2F1", "FCA667"
            ),
            VendorInfo("Amazon Technologies", DeviceCategory.IOT)
        )

        // Hikvision (CCTV)
        registerPrefixes(
            listOf(
                "001212", "101248", "105172", "1868CB", "2428FD", "2857BE", "34BDFA", "4419B6",
                "4432C8", "54C415", "64DB8B", "8CE748", "A41437", "BC5436", "C056E3", "D867C3",
                "E005C5", "ECA940"
            ),
            VendorInfo("Hikvision Digital Tech (CCTV)", DeviceCategory.CAMERA)
        )

        // Dahua (CCTV)
        registerPrefixes(
            listOf(
                "08EDB9", "14A78B", "24526A", "38AF29", "3CEF8C", "4C11BF", "502D1D", "648B20",
                "78670E", "9002A9", "A0BD1D", "BC3253", "C0395A", "E0508B"
            ),
            VendorInfo("Dahua Technology (CCTV)", DeviceCategory.CAMERA)
        )

        // Printers
        registerPrefixes(
            listOf("000085", "001E8F", "180CAC", "381A52", "708A09", "84BA3B", "9C32CE", "B40016", "CC9093"),
            VendorInfo("Canon Inc.", DeviceCategory.PRINTER)
        )
        registerPrefixes(
            listOf("000048", "0021B7", "0026AB", "44D244", "64EB8C", "AC1826", "D8492F", "E0CBEE"),
            VendorInfo("Seiko Epson Corp.", DeviceCategory.PRINTER)
        )
        registerPrefixes(
            listOf("001BA9", "008077", "30055C", "485D36", "80691A", "D0654B"),
            VendorInfo("Brother Industries", DeviceCategory.PRINTER)
        )

        // NAS
        registerPrefixes(listOf("001132"), VendorInfo("Synology NAS", DeviceCategory.SERVER_NAS))
        registerPrefixes(listOf("00089B", "245EBE"), VendorInfo("QNAP Systems", DeviceCategory.SERVER_NAS))

        // Gaming
        registerPrefixes(
            listOf("0009BF", "001F32", "0022AA", "00241E", "0025A0", "002659", "2C10C1", "342FBD", "70480F", "78A2A0", "9458CB", "9CE635", "B87826"),
            VendorInfo("Nintendo Co., Ltd.", DeviceCategory.GAMING)
        )
    }

    private fun registerPrefixes(prefixes: List<String>, info: VendorInfo) {
        for (prefix in prefixes) {
            val key = prefix.replace(":", "").replace("-", "").uppercase()
            prefixMap[key] = info
        }
    }

    /**
     * Looks up manufacturer and default category from MAC address.
     */
    fun lookupVendor(macAddress: String?): VendorInfo? {
        if (macAddress.isNullOrBlank()) return null
        val clean = macAddress.replace(":", "").replace("-", "").uppercase()
        if (clean.length < 6) return null
        val prefix = clean.substring(0, 6)
        return prefixMap[prefix]
    }

    /**
     * Smart Device Categorization Engine
     * Combines IP, Gateway, MAC vendor, Hostname, and Open Ports to determine the exact device category.
     */
    fun classifyDevice(
        ipAddress: String,
        gatewayIp: String?,
        macAddress: String?,
        hostname: String?,
        openPorts: List<Int>
    ): Pair<String, DeviceCategory> {
        val vendorInfo = lookupVendor(macAddress)
        val vendorName = vendorInfo?.vendorName ?: "Tidak diketahui"
        val hostLower = hostname?.lowercase() ?: ""
        val vendorLower = vendorName.lowercase()

        // 1. Check Gateway / Router
        if (ipAddress == gatewayIp ||
            hostLower.contains("router") || hostLower.contains("gateway") ||
            hostLower.contains("openwrt") || hostLower.contains("ddwrt") ||
            hostLower.contains("mikrotik") || hostLower.contains("unifi") ||
            hostLower.contains("tplink") || hostLower.contains("dlink") ||
            (vendorLower.contains("mikrotik") || vendorLower.contains("ubiquiti") || vendorLower.contains("d-link") || vendorLower.contains("tp-link")) && openPorts.contains(80)
        ) {
            return Pair(if (vendorName != "Tidak diketahui") vendorName else "Router Gateway", DeviceCategory.ROUTER)
        }

        // 2. Check IP Camera / CCTV
        if (openPorts.contains(554) || // RTSP
            vendorLower.contains("hikvision") || vendorLower.contains("dahua") ||
            vendorLower.contains("reolink") || vendorLower.contains("wyze") ||
            vendorLower.contains("amcrest") || vendorLower.contains("axis") ||
            hostLower.contains("camera") || hostLower.contains("cctv") ||
            hostLower.contains("ipc") || hostLower.contains("nvr") || hostLower.contains("dvr")
        ) {
            return Pair(vendorName, DeviceCategory.CAMERA)
        }

        // 3. Check Network Printer
        if (openPorts.contains(9100) || openPorts.contains(631) || // RAW print & IPP
            vendorLower.contains("canon") || vendorLower.contains("epson") ||
            vendorLower.contains("brother") || vendorLower.contains("xerox") ||
            hostLower.contains("printer") || hostLower.contains("print")
        ) {
            return Pair(vendorName, DeviceCategory.PRINTER)
        }

        // 4. Check NAS / Server
        if (openPorts.contains(5000) || openPorts.contains(5001) || // Synology DSM
            vendorLower.contains("synology") || vendorLower.contains("qnap") ||
            hostLower.contains("nas") || hostLower.contains("synology") ||
            hostLower.contains("truenas") || hostLower.contains("freenas") ||
            (openPorts.contains(22) && openPorts.contains(445) && openPorts.contains(80))
        ) {
            return Pair(vendorName, DeviceCategory.SERVER_NAS)
        }

        // 5. Check Smart TV & Media Streaming
        if (openPorts.contains(8009) || openPorts.contains(8008) || // Google Cast / DIAL
            vendorLower.contains("roku") ||
            hostLower.contains("appletv") || hostLower.contains("chromecast") ||
            hostLower.contains("bravia") || hostLower.contains("firetv") ||
            hostLower.contains("smarttv") || hostLower.contains("shield") ||
            (vendorLower.contains("lg electronics") && (hostLower.contains("tv") || hostLower.contains("webos"))) ||
            (vendorLower.contains("sony") && hostLower.contains("tv"))
        ) {
            return Pair(vendorName, DeviceCategory.SMART_TV)
        }

        // 6. Check IoT & Smart Home
        if (vendorLower.contains("espressif") || vendorLower.contains("tuya") ||
            vendorLower.contains("sonoff") || vendorLower.contains("shelly") ||
            vendorLower.contains("raspberry") ||
            hostLower.contains("esp_") || hostLower.contains("esp8266") ||
            hostLower.contains("esp32") || hostLower.contains("tasmota") ||
            hostLower.contains("smartplug") || hostLower.contains("sonoff") ||
            hostLower.contains("hue") || hostLower.contains("wemo") ||
            (vendorLower.contains("amazon") && hostLower.contains("echo"))
        ) {
            return Pair(vendorName, DeviceCategory.IOT)
        }

        // 7. Check Gaming Console
        if (vendorLower.contains("nintendo") ||
            hostLower.contains("playstation") || hostLower.contains("ps4") || hostLower.contains("ps5") ||
            hostLower.contains("xbox") || hostLower.contains("switch")
        ) {
            return Pair(vendorName, DeviceCategory.GAMING)
        }

        // 8. Check Computers / Laptops
        if (openPorts.contains(3389) || // Windows RDP
            vendorLower.contains("intel") || vendorLower.contains("dell") ||
            vendorLower.contains("lenovo") ||
            hostLower.contains("desktop") || hostLower.contains("laptop") ||
            hostLower.contains("macbook") || hostLower.contains("imac") ||
            hostLower.contains("pc") || hostLower.contains("thinkpad")
        ) {
            return Pair(vendorName, DeviceCategory.COMPUTER)
        }

        // 9. Check Smartphones / Tablets
        if (hostLower.contains("iphone") || hostLower.contains("ipad") ||
            hostLower.contains("galaxy") || hostLower.contains("pixel") ||
            hostLower.contains("android") || hostLower.contains("xiaomi") ||
            hostLower.contains("redmi") || hostLower.contains("oppo") ||
            hostLower.contains("vivo") || hostLower.contains("realme")
        ) {
            return Pair(vendorName, DeviceCategory.SMARTPHONE)
        }

        // 10. Fallback to vendor default category or UNKNOWN
        if (vendorInfo != null) {
            return Pair(vendorInfo.vendorName, vendorInfo.defaultCategory)
        }

        return Pair(vendorName, DeviceCategory.UNKNOWN)
    }
}
