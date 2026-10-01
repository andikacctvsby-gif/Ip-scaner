package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class DeviceCategory(
    val title: String,
    val description: String,
    val color: Color
) {
    ROUTER(
        title = "Router & Gateway",
        description = "Gerbang jaringan / Titik akses WiFi",
        color = CategoryRouter
    ),
    SMARTPHONE(
        title = "Ponsel Cerdas / Tablet",
        description = "Perangkat seluler Android / iOS",
        color = CategoryPhone
    ),
    COMPUTER(
        title = "Komputer / Laptop",
        description = "PC Windows, Mac, atau Linux",
        color = CategoryComputer
    ),
    IOT(
        title = "Perangkat IoT & Smart Home",
        description = "Lampu pintar, saklar, steker pintar, mikrokontroler",
        color = CategoryIot
    ),
    SMART_TV(
        title = "Smart TV & Streaming",
        description = "Android TV, Chromecast, Apple TV, Smart TV",
        color = CategoryTv
    ),
    PRINTER(
        title = "Printer Jaringan",
        description = "Printer Wi-Fi / Scanner dokumen",
        color = CategoryPrinter
    ),
    CAMERA(
        title = "Kamera IP / CCTV",
        description = "Kamera pengawas keamanan / Web stream RTSP",
        color = CategoryCamera
    ),
    SERVER_NAS(
        title = "Server & Penyimpanan NAS",
        description = "Server jaringan, media server, NAS",
        color = CategoryServer
    ),
    GAMING(
        title = "Konsol Game",
        description = "PlayStation, Xbox, Nintendo Switch",
        color = CategoryGaming
    ),
    UNKNOWN(
        title = "Perangkat Umum",
        description = "Perangkat jaringan terhubung",
        color = CategoryUnknown
    );

    companion object {
        fun fromString(value: String?): DeviceCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
