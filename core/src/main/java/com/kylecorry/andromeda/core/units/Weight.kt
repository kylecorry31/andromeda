package com.kylecorry.andromeda.core.units

import kotlin.math.absoluteValue

data class Weight(val weight: Float, val units: WeightUnits) {
    fun convertTo(newUnits: WeightUnits): Weight {
        if (units == newUnits) {
            return this
        }
        val grams = weight * units.grams
        return Weight(grams / newUnits.grams, newUnits)
    }

    operator fun plus(other: Weight): Weight {
        val otherInUnits = other.convertTo(units)
        return Weight(weight + otherInUnits.weight, units)
    }

    operator fun times(amount: Number): Weight {
        return Weight(weight * amount.toFloat().absoluteValue, units)
    }
}
