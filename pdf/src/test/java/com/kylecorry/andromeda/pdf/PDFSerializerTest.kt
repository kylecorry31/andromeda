package com.kylecorry.andromeda.pdf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayOutputStream
import java.util.stream.Stream


internal class PDFSerializerTest {

    @Test
    fun generate() {
        val pdf = """%PDF-1.3
1 0 obj
<</Type /Catalog /Pages 2 0 R>>
endobj
2 0 obj
<</Type /Pages /Kids [3 0 R]>>
endobj
3 0 obj
<</Type /Page /Parent 2 0 R /MediaBox [0 0 200 200] /Contents [4 0 R]>>
endobj
4 0 obj
<</Length 41>>
stream
BT /F1 12 Tf 0 150 Td (Hello World!)Tj ET
endstream
endobj
xref
0 4
0000000000 65535 f
0000000009 00000 n
0000000056 00000 n
0000000102 00000 n
0000000189 00000 n
trailer
<<
/Size 4
/Root 1 0 R
>>
startxref
278
%%EOF"""
        val objects = listOf(
            catalog(1, 2),
            pages(2, listOf(3)),
            page(3, 2, 200, 200, listOf(4)),
            text(4, "Hello World!", 0, 150, 12)
        )

        val serializer = PDFSerializer()
        val stream = ByteArrayOutputStream()
        serializer.serialize(objects, stream)
        assertEquals(pdf, stream.toString())
    }

    @ParameterizedTest
    @MethodSource("providePdf")
    fun deserialize(pdf: String, ignoreStreams: Boolean, expected: List<PDFValue.PDFObject>) {
        val serializer = PDFSerializer(ignoreStreams)
        val actual = serializer.deserialize(pdf.byteInputStream())
        assertEquals(expected.size, actual.size)
        for (i in actual.indices) {
            assertEquals(expected[i], actual[i])
        }
    }

    private fun assertEquals(expected: PDFValue.PDFObject, actual: PDFValue.PDFObject) {
        assertEquals(expected.id, actual.id)
        assertEquals(expected.content, actual.content)
    }


    companion object {

        @JvmStatic
        fun providePdf(): Stream<Arguments> {

            val withStreams = listOf(
                PDFValue.PDFObject(
                    3, 0,
                    listOf(
                        PDFValue.PDFDictionary(
                            mapOf(
                                PDFValue.PDFName("/Author") to PDFValue.PDFString("Kyle"),
                                PDFValue.PDFName("/Creator") to PDFValue.PDFString("CreatedBy")
                            )
                        )
                    ),
                ),
                PDFValue.PDFObject(
                    4, 0,
                    listOf(
                        PDFValue.PDFDictionary(
                            mapOf(
                                PDFValue.PDFName("/Type") to PDFValue.PDFName("/Measure"),
                                PDFValue.PDFName("/Subtype") to PDFValue.PDFName("/GEO"),
                                PDFValue.PDFName("/Reference") to PDFValue.PDFIndirectObject(3, 0)
                            )
                        ),
                        PDFValue.PDFStream("Test stream\nHere".toByteArray())
                    ),
                ),
            )

            val withoutStreams = listOf(
                PDFValue.PDFObject(
                    3, 0,
                    listOf(
                        PDFValue.PDFDictionary(
                            mapOf(
                                PDFValue.PDFName("/Author") to PDFValue.PDFString("Kyle"),
                                PDFValue.PDFName("/Creator") to PDFValue.PDFString("CreatedBy")
                            )
                        )
                    ),
                ),
                PDFValue.PDFObject(
                    4, 0,
                    listOf(
                        PDFValue.PDFDictionary(
                            mapOf(
                                PDFValue.PDFName("/Type") to PDFValue.PDFName("/Measure"),
                                PDFValue.PDFName("/Subtype") to PDFValue.PDFName("/GEO"),
                                PDFValue.PDFName("/Reference") to PDFValue.PDFIndirectObject(3, 0)
                            )
                        )
                    ),
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