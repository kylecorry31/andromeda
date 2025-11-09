package com.kylecorry.andromeda.csv

import com.kylecorry.andromeda.core.io.writeAll
import com.kylecorry.luna.streams.readText
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream

object CSVConvert {

    /*
        https://www.rfc-editor.org/rfc/rfc4180#page-2

        1. Rows are deliminated by CRLF
        2. CRLF is optional on the last row
        3. The first line may be a header, but it is indistinguishable from the other rows.
        4a. Each row should contain one or more columns separated by commas.
        4b. Each row should contain the same number of columns.
        4c. Spaces are part of the column and should not be ignored.
        4d. The last column in the row does not have a comma at the end.
        5a. Each column can be enclosed in double quotes.
        5b. If a field is not enclosed in double quotes, then it can not contain a double quote.
        6. Fields containing CRLF, double quotes, and commas should be enclosed in double quotes.
        7.If double quotes are used to enclose the column, then double quotes in the column should be escaped using a second double quote.
     */

    fun toCSV(stream: OutputStream, data: List<List<Any?>>) {
        for (row in data){
            for (i in row.indices){
                val cell = row[i]?.toString() ?: ""
                // #5a, #6
                val areQuotesRequired = cell.contains(",") || cell.contains("\"") || cell.contains("\n") || cell.contains("\r")
                if (areQuotesRequired){
                    stream.write('"'.code)
                }
                // #7
                stream.writeAll(cell.replace("\"", "\"\"").toByteArray())
                if (areQuotesRequired){
                    stream.write('"'.code)
                }
                // #4a, #4d
                if (i != row.lastIndex){
                    stream.write(','.code)
                }
            }
            // #1
            stream.write("\r\n".toByteArray())
        }
    }

    fun toCSV(data: List<List<Any?>>): String {
        val bytes = ByteArrayOutputStream()
        toCSV(bytes, data)
        return String(bytes.toByteArray())
    }

    fun parse(input: InputStream): List<List<String>> {
        // TODO: Byte by byte parsing
        return parse(input.readText())
    }

    fun parse(csv: String): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        var currentRow = mutableListOf<String>()
        val currentCell = StringBuilder()
        var insideQuotes = false
        var i = 0
        while (i < csv.length) {
            val char = csv[i]
            when (char) {
                '"' -> {
                    // #5a or #6/7
                    if (insideQuotes && i < csv.lastIndex && csv[i + 1] == '"') {
                        currentCell.append('"')
                        i++
                    } else {
                        insideQuotes = !insideQuotes
                    }
                }
                ',' -> {
                    // #4a or #6
                    if (insideQuotes) {
                        currentCell.append(char)
                    } else {
                        currentRow.add(currentCell.toString())
                        currentCell.clear()
                    }
                }
                '\r', '\n' -> {
                    // #1 or #6
                    if (insideQuotes){
                        currentCell.append(char)
                    } else {
                        if (i < csv.lastIndex && csv[i + 1] == '\n') {
                            // Also skip the LF in CRLF
                            i++
                        }
                        currentRow.add(currentCell.toString())
                        rows.add(currentRow)
                        currentRow = mutableListOf()
                        currentCell.clear()
                    }
                }
                else -> {
                    currentCell.append(char)
                }
            }
            i++
        }

        // Add the last cell and row if necessary
        if (currentCell.isNotEmpty()) {
            currentRow.add(currentCell.toString())
        }

        if (currentRow.isNotEmpty()) {
            rows.add(currentRow)
        }

        return rows
    }

}