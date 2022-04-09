package com.kylecorry.andromeda.pdf

import com.kylecorry.sol.units.Coordinate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


internal class PDFGeneratorTest {

    @Test
    fun generate() {
        val pdf = """%PDF-1.3
1 0 obj
<<
/Type /Catalog
/Pages 2 0 R
>>
endobj
2 0 obj
<<
/Type /Pages
/Kids [3 0 R]
>>
endobj
3 0 obj
<<
/Type /Page
/Parent 2 0 R
/MediaBox [0 0 200 200]
/Contents [4 0 R]
>>
endobj
4 0 obj
<<
/Length 41
>>
stream
BT /F1 12 Tf 0 150 Td (Hello World!)Tj ET
endstream
endobj
xref
0 4
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000106 00000 n
0000000195 00000 n
trailer
<<
/Size 4
/Root 1 0 R
>>
startxref
286
%%EOF"""
        val objects = listOf(
            catalog("1 0", "2 0"),
            pages("2 0", listOf("3 0")),
            page("3 0", "2 0", 200, 200, listOf("4 0")),
            viewport("5 0", "6 0", bbox(0, 0, 200, 200)),
            text("4 0", "Hello World!", 0, 150, 12),
            geo(
                "6 0",
                gpts = listOf(
                    Coordinate(41.8895, -71.72549),
                    Coordinate(41.924248, -71.72549),
                    Coordinate(41.924248, -71.68764),
                    Coordinate(41.8895, -71.68764)
                )
            )
        )

        val generator = PDFGenerator()
        val actual = generator.toPDF(objects)
        assertEquals(pdf, actual)

    }
}