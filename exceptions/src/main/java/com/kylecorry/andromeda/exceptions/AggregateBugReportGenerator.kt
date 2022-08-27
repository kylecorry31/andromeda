package com.kylecorry.andromeda.exceptions

import android.content.Context
import com.kylecorry.andromeda.core.tryOrDefault

class AggregateBugReportGenerator(private val generators: List<IBugReportGenerator>) :
    IBugReportGenerator {
    override fun generate(context: Context, throwable: Throwable): String {
        return generators.joinToString("\n") {
            tryOrDefault("") {
                it.generate(context, throwable)
            }
        }
    }
}