package com.kylecorry.andromeda.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.ByteArrayOutputStream

object CSVConvert {
    fun toCSV(data: List<List<Any?>>): String {
        val bytes = ByteArrayOutputStream()
        csvWriter().writeAll(data, bytes)
        return String(bytes.toByteArray())
    }

    fun parse(csv: String): List<List<String>> {
        return csvReader().readAll(csv)
    }

}