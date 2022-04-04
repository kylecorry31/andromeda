package com.kylecorry.andromeda.pdf

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class PDFParserTest {

    @ParameterizedTest
    @MethodSource("providePdf")
    fun parse(pdf: String, ignoreStreams: Boolean, expected: List<PDFObject>) {
        val parser = PDFParser()
        val actual = parser.parse(pdf.byteInputStream(), ignoreStreams)
        assertEquals(expected, actual)
    }


    companion object {

        @JvmStatic
        fun providePdf(): Stream<Arguments> {

            val withStreams = listOf(
                PDFObject("3 0", listOf("/Author Kyle", "/Creator CreatedBy"), emptyList()),
                PDFObject(
                    "4 0",
                    listOf("/Type /Measure", "/Subtype /GEO", "/Reference 3 0 R"),
                    listOf("Test streamHere")
                ),
            )

            val withoutStreams = listOf(
                PDFObject("3 0", listOf("/Author Kyle", "/Creator CreatedBy"), emptyList()),
                PDFObject(
                    "4 0",
                    listOf("/Type /Measure", "/Subtype /GEO", "/Reference 3 0 R"),
                    emptyList()
                ),
            )

            return Stream.of(
                Arguments.of(pdf, false, withStreams),
                Arguments.of(formattedPDF, false, withStreams),
                Arguments.of(pdf, true, withoutStreams),
                Arguments.of(formattedPDF, true, withoutStreams),
            )
        }


        private val pdf = """%PDF-1.6
%����
3 0 obj
<< /Author Kyle /Creator CreatedBy >>
endobj
4 0 obj
<< /Type /Measure /Subtype /GEO /Reference 3 0 R >>
stream
Test stream
Here
endstream

endobj"""

        private val formattedPDF = """%PDF-1.6
%����
3 0 obj
<<
/Author Kyle
/Creator CreatedBy
>>
endobj
4 0 obj
<<
/Type /Measure
/Subtype /GEO
/Reference 3 0 R
>>
stream
Test stream
Here
endstream

endobj"""
    }

}