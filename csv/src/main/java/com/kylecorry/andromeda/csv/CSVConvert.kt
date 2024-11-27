package com.kylecorry.andromeda.csv

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader

object CSVConvert {
    fun toCSV(data: List<List<Any?>>): String {
        return data.joinToString("\r\n") { row ->
            row.joinToString(",") { cell ->
                val escaped = cell.toString().replace("\"", "\"\"")
                if (escaped.contains(",") || escaped.contains("\"") || escaped.contains("\n")) {
                    "\"$escaped\""
                } else {
                    escaped
                }
            }
        } + "\r\n"
    }

    fun parse(csv: String): List<List<String>> {
        return csvReader().readAll(csv)
    }

}