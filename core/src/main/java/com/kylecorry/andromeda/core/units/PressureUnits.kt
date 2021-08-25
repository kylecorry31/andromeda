package com.kylecorry.andromeda.core.units

enum class PressureUnits(val id: Int, val hpa: Float) {
    Hpa(1, 1f),
    Mbar(2, 1f),
    Inhg(3, 1 / 0.02953f),
    Psi(4, 1 / 0.0145037738f),
    MmHg(5, 1.3332239f)
}