package com.kylecorry.andromeda.pdf

import com.kylecorry.andromeda.core.units.PixelCoordinate
import com.kylecorry.andromeda.wkt.CRSWellKnownTextConvert
import com.kylecorry.andromeda.wkt.WKTNumber
import com.kylecorry.andromeda.wkt.WKTString
import com.kylecorry.andromeda.wkt.getSection
import com.kylecorry.sol.units.Coordinate
import java.io.InputStream
import kotlin.math.absoluteValue

class GeospatialPDFParser {

    private val pdfParser = PDFParser()

    fun parse(pdf: InputStream): GeospatialPDFMetadata? {
        val objects = pdfParser.parse(pdf, ignoreStreams = true)

        // TODO: Support multiple pages - pages are linked to viewports through the VP array
        val page = getPage(objects)
        val viewports = getViewports(objects)

        for (viewport in viewports) {
            val vp = viewport.getProperty<PDFValue.PDFDictionary>("/vp")
            val viewportProperties = viewport.getProperties()
            val vpMeasure = vp?.getProperties("/Measure", objects)
            // If the VP has a larger bounding box and it has a geo measure, use that instead
            val actingVP =
                if (vp != null && vpMeasure != null && isGeoMeasure(vpMeasure) && getBoundingBoxArea(
                        vp
                    ) > getBoundingBoxArea(viewportProperties)
                ) {
                    vp
                } else {
                    viewportProperties
                }


            val mediabox =
                (page ?: viewport).getDoubleArray("/mediabox") ?: vp?.getDoubleArray("/mediabox")
                ?: emptyList()

            val measure = if (actingVP == vp) {
                vpMeasure
            } else {
                viewportProperties.getProperties("/Measure", objects)
            }
            if (measure == null || !isGeoMeasure(measure)) {
                return null
            }

            val gcs = measure.getProperties("/GCS", objects)

            return getMetadata(mediabox, actingVP, measure, gcs) ?: continue
        }

        return null
    }


    private fun getMetadata(
        mediabox: List<Double>,
        viewport: PDFValue.PDFDictionary,
        measure: PDFValue.PDFDictionary,
        gcs: PDFValue.PDFDictionary?
    ): GeospatialPDFMetadata? {

        val bbox = viewport.getDoubleArray("/bbox")
        var lpts = measure.getDoubleArray("/lpts")
        val gpts = measure.getDoubleArray("/gpts")

        if (mediabox.size < 4 || bbox.size < 4) {
            return null
        }

        if (lpts.size < gpts.size) {
            lpts = listOf(0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 1.0)
        }
        val gcsValue = gcs?.getAs<PDFValue.PDFString>("/wkt")?.value ?: ""

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
        val wkt = CRSWellKnownTextConvert.toWKT(gcs.substring(1, gcs.length)) ?: return null
        val datum = wkt.getSection("datum")?.get<WKTString>(0)?.value ?: "WGS 84"
        val spheroidSec = wkt.getSection("spheroid")
        val primeMeridian = wkt.getSection("primem")?.get<WKTNumber>(1)?.value ?: 0.0
        val projection = wkt.getSection("projection")?.get<WKTString>(0)?.value ?: return null

        val spheroidName = spheroidSec?.get<WKTString>(0)?.value ?: "WGS 84"
        val semiMajorAxis = spheroidSec?.get<WKTNumber>(1)?.value?.toFloat() ?: 6378137f
        val inverseFlattening = spheroidSec?.get<WKTNumber>(2)?.value?.toFloat() ?: 298.2572229f

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

    private fun getViewports(objects: List<PDFValue.PDFObject>): List<PDFValue.PDFObject> {
        return objects.getByProperty("/Type", PDFValue.PDFName("/Viewport"))
    }

    private fun getPage(objects: List<PDFValue.PDFObject>): PDFValue.PDFObject? {
        return objects.getByProperty("/Type", PDFValue.PDFName("/Page")).firstOrNull()
    }

    private fun isGeoMeasure(measure: PDFValue.PDFDictionary): Boolean {
        return measure.hasValue("/subtype", PDFValue.PDFName("/GEO"))
    }

    private fun List<PDFValue.PDFObject>.getByProperty(
        property: String,
        value: PDFValue
    ): List<PDFValue.PDFObject> {
        return this.filter {
            it.getProperty<PDFValue>(property) == value
        }
    }

    private fun List<PDFValue.PDFObject>.getById(id: Int): PDFValue.PDFObject? {
        return this.firstOrNull { it.id == id }
    }

    private fun PDFValue.PDFObject.getDoubleArray(property: String): List<Double>? {
        return getProperty<PDFValue.PDFArray>(property)?.values?.mapNotNull { (it as? PDFValue.PDFNumber)?.value?.toDouble() }
    }

    private fun PDFValue.PDFDictionary.getDoubleArray(property: String): List<Double> {
        return getAs<PDFValue.PDFArray>(property)?.values?.mapNotNull { (it as? PDFValue.PDFNumber)?.value?.toDouble() }
            ?: emptyList()
    }

    private fun getBoundingBoxArea(viewport: PDFValue.PDFDictionary): Float {
        val bbox = viewport.getDoubleArray("/bbox")
        val width = (bbox[2] - bbox[0]).absoluteValue
        val height = (bbox[3] - bbox[1]).absoluteValue
        return (width * height).toFloat()
    }

    private fun PDFValue.PDFObject.getProperties(): PDFValue.PDFDictionary {
        return content.firstOrNull { it is PDFValue.PDFDictionary } as? PDFValue.PDFDictionary
            ?: PDFValue.PDFDictionary(emptyMap())
    }

    private fun PDFValue.PDFDictionary.getProperties(
        key: String,
        objects: List<PDFValue.PDFObject>
    ): PDFValue.PDFDictionary? {
        val property = get(key)
        return when (property) {
            is PDFValue.PDFIndirectObject -> {
                objects.getById(property.id)?.getProperties()
            }

            is PDFValue.PDFDictionary -> {
                property
            }

            else -> {
                null
            }
        }
    }
}