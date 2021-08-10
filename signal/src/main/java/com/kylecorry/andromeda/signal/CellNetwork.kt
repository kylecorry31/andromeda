package com.kylecorry.andromeda.signal

enum class CellNetwork(val id: Int, val minDbm: Int, val maxDbm: Int) {
    Nr(1, -140, -44),
    Lte(2, -140, -44),
    Cdma(3, -100, -75),
    Wcdma(4, -113, -51),
    Gsm(5, -113, -51),
    Tdscdma(6, -120, -24)
}