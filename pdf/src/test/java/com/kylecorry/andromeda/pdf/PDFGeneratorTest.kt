package com.kylecorry.andromeda.pdf

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
/Resources 4 0 R
/MediaBox [0 0 500 800]
/Contents 6 0 R
>>
endobj
4 0 obj
<<
/Font <</F1 5 0 R>>
>>
endobj
5 0 obj
<<
/Type /Font
/Subtype /Type1
/BaseFont /Helvetica
>>
endobj
6 0 obj
<<
/Length 44
>>
stream
BT /F1 24 Tf 175 720 Td (Hello World!)Tj ET
endstream
endobj
xref
0 6
0000000000 65535 f
0000000009 00000 n
0000000058 00000 n
0000000106 00000 n
0000000210 00000 n
0000000251 00000 n
0000000321 00000 n
trailer
<<
/Size 6
/Root 1 0 R
>>
startxref
414
%%EOF"""

        val objects = listOf(
            PDFObject(
                "1 0", listOf(
                    "/Type /Catalog",
                    "/Pages 2 0 R"
                ), emptyList()
            ),
            PDFObject(
                "2 0", listOf(
                    "/Type /Pages",
                    "/Kids [3 0 R]"
                ), emptyList()
            ),
            PDFObject(
                "3 0", listOf(
                    "/Type /Page",
                    "/Parent 2 0 R",
                    "/Resources 4 0 R",
                    "/MediaBox [0 0 500 800]",
                    "/Contents 6 0 R"
                ), emptyList()
            ),
            PDFObject(
                "4 0", listOf(
                    "/Font <</F1 5 0 R>>"
                ), emptyList()
            ),
            PDFObject(
                "5 0", listOf(
                    "/Type /Font",
                    "/Subtype /Type1",
                    "/BaseFont /Helvetica"
                ), emptyList()
            ),
            PDFObject(
                "6 0", listOf(
                    "/Length 44"
                ), listOf("BT /F1 24 Tf 175 720 Td (Hello World!)Tj ET")
            ),
        )

        val generator = PDFGenerator()
        val actual = generator.toPDF(objects)
        println(actual)
    }
}