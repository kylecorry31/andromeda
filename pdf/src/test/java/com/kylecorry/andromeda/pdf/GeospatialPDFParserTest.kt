package com.kylecorry.andromeda.pdf

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

internal class GeospatialPDFParserTest {

    @ParameterizedTest
    @MethodSource("provideMissingWkt")
    fun parseWithoutWkt(measureGcs: String, gcsObject: String) {
        val metadata = GeospatialPDFParser().parse(pdf(measureGcs, gcsObject).byteInputStream())

        assertEquals(4, metadata?.points?.size)
        assertNull(metadata?.projection)
    }

    @Test
    fun parseWithWkt() {
        val wkt = "PROJCS[\"WGS 84 / UTM zone 33N\",GEOGCS[\"WGS 84\",DATUM[\"WGS_1984\"," +
                "SPHEROID[\"WGS 84\",6378137,298.257223563]],PRIMEM[\"Greenwich\",0]]," +
                "PROJECTION[\"Transverse_Mercator\"]]"
        val metadata = GeospatialPDFParser().parse(
            pdf("/GCS 4 0 R", "4 0 obj\n<< /Type /PROJCS /WKT ($wkt) >>\nendobj").byteInputStream()
        )

        assertEquals(4, metadata?.points?.size)
        assertEquals("Transverse_Mercator", metadata?.projection?.projection)
    }

    private fun pdf(measureGcs: String, gcsObject: String) = """%PDF-1.7
1 0 obj
<< /Type /Page /MediaBox [0 0 612 792] /VP [2 0 R] >>
endobj
2 0 obj
<< /Type /Viewport /BBox [0 0 612 792] /Measure 3 0 R >>
endobj
3 0 obj
<< /Type /Measure /Subtype /GEO /Bounds [0 1 0 0 1 0 1 1] /GPTS [10 20 11 20 11 21 10 21] /LPTS [0 1 0 0 1 0 1 1] $measureGcs >>
endobj
$gcsObject
%%EOF
"""

    companion object {
        @JvmStatic
        fun provideMissingWkt(): Stream<Arguments> {
            return Stream.of(
                Arguments.of("", ""),
                Arguments.of("/GCS 4 0 R", "4 0 obj\n<< /Type /PROJCS /EPSG 32633 >>\nendobj"),
                Arguments.of("/GCS 4 0 R", "4 0 obj\n<< /Type /PROJCS /WKT () >>\nendobj"),
            )
        }
    }
}
