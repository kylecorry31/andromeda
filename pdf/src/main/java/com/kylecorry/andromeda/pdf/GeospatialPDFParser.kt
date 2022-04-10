package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.andromeda.wkt.CRSWellKnownTextConvert
import com.kylecorry.andromeda.wkt.getSection
import com.kylecorry.sol.units.Coordinate
import java.io.InputStream
import kotlin.math.absoluteValue

class GeospatialPDFParser {

    private val pdfParser = PDFParser()

    fun parse(pdf: InputStream): GeospatialPDFMetadata? {
        val objects = pdfParser.parse(pdf, ignoreStreams = true)

        // TODO: Support multiple pages - pages are linked to viewports through the VP array
        val page = getPage(objects) ?: return null
        val viewports = getViewports(objects)

        for (viewport in viewports) {
            val measure = objects.getById(viewport["/measure"] ?: "") ?: continue
            if (!isGeoMeasure(measure)) {
                return null
            }
            val gcs = objects.getById(measure["/gcs"] ?: "")
            return getMetadata(page, viewport, measure, gcs) ?: continue
        }

        return null
    }


    private fun getMetadata(
        page: PDFObject,
        viewport: PDFObject,
        measure: PDFObject,
        gcs: PDFObject?
    ): GeospatialPDFMetadata? {

        val mediabox = page.getArray("/mediabox").map { it.toDouble() }
        val bbox = viewport.getArray("/bbox").map { it.toDouble() }
        var lpts = measure.getArray("/lpts").map { it.toDouble() }
        val gpts = measure.getArray("/gpts").map { it.toDouble() }

        if (mediabox.size < 4 || bbox.size < 4) {
            return null
        }

        if (lpts.size < gpts.size) {
            lpts = listOf(0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0)
        }
        val gcsValue = gcs?.get("/wkt") ?: ""

        val pageHeight = mediabox[3]

        // left, top, right, bottom
        val left = bbox[0]
        val inverted = bbox[1] > bbox[3]
        val top = pageHeight - bbox[if (inverted) 1 else 3]
        val width = (bbox[2] - bbox[0]).absoluteValue
        val height = (bbox[3] - bbox[1]).absoluteValue

        val coordinates = mutableListOf<Pair<PixelCoordinate, Coordinate>>()

        for (i in 1 until gpts.size step 2) {
            val latitude = gpts[i - 1]
            val longitude = gpts[i]

            val pctY = lpts[i]
            val pctX = lpts[i - 1]

            val y = pctY * height + top
            val x = pctX * width + left

            coordinates.add(
                PixelCoordinate(x.toFloat(), y.toFloat()) to Coordinate(
                    latitude,
                    longitude
                )
            )
        }

        return GeospatialPDFMetadata(coordinates, getProjection(gcsValue))
    }

    private fun getProjection(gcs: String): ProjectedCoordinateSystem? {
        val wkt = CRSWellKnownTextConvert.toWKT(gcs) ?: return null
        val datum = wkt.getSection("datum")?.get<com.kylecorry.andromeda.wkt.WKTString>(0)?.value ?: return null
        val spheroidSec = wkt.getSection("spheroid")
        val primeMeridian = wkt.getSection("primem")?.get<com.kylecorry.andromeda.wkt.WKTNumber>(1)?.value ?: 0.0
        val projection = wkt.getSection("projection")?.get<com.kylecorry.andromeda.wkt.WKTString>(0)?.value ?: return null

        val spheroidName = spheroidSec?.get<com.kylecorry.andromeda.wkt.WKTString>(0)?.value ?: "WGS 84"
        val semiMajorAxis = spheroidSec?.get<com.kylecorry.andromeda.wkt.WKTNumber>(1)?.value?.toFloat() ?: 6378137f
        val inverseFlattening = spheroidSec?.get<com.kylecorry.andromeda.wkt.WKTNumber>(2)?.value?.toFloat() ?: 298.2572229f

        return ProjectedCoordinateSystem(
            GeographicCoordinateSystem(
                Datum(
                    datum,
                    Spheroid(spheroidName, semiMajorAxis, inverseFlattening)
                ),
                primeMeridian
            ),
            projection
        )
    }

    private fun getViewports(objects: List<PDFObject>): List<PDFObject> {
        return objects.getByProperty("/Type", "/viewport")
    }

    private fun getPage(objects: List<PDFObject>): PDFObject? {
        return objects.getByProperty("/Type", "/page").firstOrNull()
    }

    private fun isGeoMeasure(measure: PDFObject): Boolean {
        return measure["/subtype"]?.contentEquals("/geo", ignoreCase = true) ?: false
    }
}