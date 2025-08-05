package com.kylecorry.andromeda.pdf

import android.graphics.Bitmap
import com.kylecorry.andromeda.wkt.CRSWellKnownTextConvert
import com.kylecorry.andromeda.wkt.WKTNumber
import com.kylecorry.andromeda.wkt.WKTSection
import com.kylecorry.andromeda.wkt.WKTString
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate
import java.io.ByteArrayOutputStream

fun obj(id: Int, vararg contents: PDFValue?): PDFValue.PDFObject {
    return PDFValue.PDFObject(id, 0, contents.toList().filterNotNull())
}

fun dict(vararg entries: Pair<PDFValue.PDFName, PDFValue>?): PDFValue.PDFDictionary {
    return PDFValue.PDFDictionary(entries.filterNotNull().toMap())
}

fun name(name: String): PDFValue.PDFName {
    return PDFValue.PDFName(name)
}

fun string(value: String): PDFValue.PDFString {
    return PDFValue.PDFString(value)
}

fun number(value: Number): PDFValue.PDFNumber {
    return PDFValue.PDFNumber(value)
}

fun array(vararg values: PDFValue): PDFValue.PDFArray {
    return PDFValue.PDFArray(values.toList())
}

fun ref(objectId: Int): PDFValue.PDFIndirectObject {
    return PDFValue.PDFIndirectObject(objectId)
}

fun stream(bytes: ByteArray): PDFValue.PDFStream {
    return PDFValue.PDFStream(bytes)
}

fun stream(string: String): PDFValue.PDFStream {
    return PDFValue.PDFStream(string.toByteArray())
}

fun catalog(objectId: Int, pagesObjectReference: Int): PDFValue.PDFObject {
    return obj(
        objectId,
        dict(
            name("/Type") to name("/Catalog"),
            name("/Pages") to ref(pagesObjectReference)
        )
    )
}

fun pages(objectId: Int, childIds: List<Int>): PDFValue.PDFObject {
    return obj(
        objectId,
        dict(
            name("/Type") to name("/Pages"),
            name("/Kids") to array(*childIds.map { ref(it) }.toTypedArray())
        )
    )
}

fun page(
    id: Int,
    parent: Int,
    width: Int,
    height: Int,
    contentIds: List<Int>,
    viewportIds: List<Int> = emptyList(),
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return obj(
        id,
        dict(
            name("/Type") to name("/Page"),
            name("/Parent") to ref(parent),
            name("/MediaBox") to array(
                number(0), number(0), number(width), number(height)
            ),
            name("/Contents") to array(*contentIds.map { ref(it) }.toTypedArray()),
            if (viewportIds.isEmpty()) null else name("/VP") to array(*viewportIds.map { ref(it) }
                .toTypedArray()),
            *properties.properties.toList().toTypedArray()
        )
    )
}

fun viewport(
    id: Int,
    measureId: Int,
    bbox: DoubleArray,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return obj(
        id,
        dict(
            name("/Type") to name("/Viewport"),
            name("/Measure") to ref(measureId),
            name("/BBox") to array(*bbox.map { number(it) }.toTypedArray()),
            *properties.properties.toList().toTypedArray()
        )
    )
}

fun geo(
    id: Int,
    gpts: List<Coordinate>,
    lpts: List<Vector2> = listOf(
        Vector2(0f, 1f),
        Vector2(0f, 0f),
        Vector2(1f, 0f),
        Vector2(1f, 1f)
    ),
    gcsId: Int? = null,
    bounds: List<Vector2> = listOf(
        Vector2(0f, 1f),
        Vector2(0f, 0f),
        Vector2(1f, 0f),
        Vector2(1f, 1f)
    ),
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return obj(
        id,
        dict(
            name("/Type") to name("/Measure"),
            name("/Subtype") to name("/GEO"),
            name("/Bounds") to array(*bounds.flatMap { listOf(number(it.x), number(it.y)) }
                .toTypedArray()),
            name("/LPTS") to array(*lpts.flatMap { listOf(number(it.x), number(it.y)) }
                .toTypedArray()),
            name("/GPTS") to array(*gpts.flatMap {
                listOf(
                    number(it.latitude),
                    number(it.longitude)
                )
            }.toTypedArray()),
            if (gcsId != null) name("/GCS") to ref(gcsId) else null,
            *properties.properties.toList().toTypedArray()
        )
    )
}

fun gcs(
    id: Int,
    projcs: ProjectedCoordinateSystem,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    val datum = projcs.geographic.datum
    val spheroid = datum.spheroid

    val wkt = WKTSection(
        "PROJCS", listOf(
            WKTString("WGS 84"),
            WKTSection(
                "GEOGCS", listOf(
                    WKTString("WGS 84"),
                    WKTSection(
                        "DATUM", listOf(
                            WKTString(datum.name),
                            WKTSection(
                                "SPHEROID", listOf(
                                    WKTString(spheroid.name),
                                    WKTNumber(spheroid.semiMajorAxis.toDouble()),
                                    WKTNumber(spheroid.inverseFlattening.toDouble())
                                )
                            )
                        )
                    )
                )
            ),
            WKTSection(
                "PROJECTION", listOf(
                    WKTString(projcs.projection)
                )
            )
        )
    )

    return obj(
        id,
        dict(
            name("/Type") to name("/PROJCS"),
            name("/WKT") to string(CRSWellKnownTextConvert.fromWKT(wkt)),
            *properties.properties.toList().toTypedArray()
        )
    )
}

fun stream(
    id: Int,
    contents: String,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return stream(id, contents.toByteArray(), properties)
}

fun stream(
    id: Int,
    contents: ByteArray,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return obj(
        id,
        dict(
            name("/Length") to number(contents.size),
            *properties.properties.toList().toTypedArray()
        ),
        stream(contents)
    )
}

private fun dctImage(image: Bitmap, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

fun image(
    id: Int,
    image: Bitmap,
    x: Int = 0,
    y: Int = 0,
    destWidth: Int = image.width,
    destHeight: Int = image.height,
    quality: Int = 100,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {

    val stream = ByteArrayOutputStream()
    val writer = stream.bufferedWriter()
    writer.append("Q $destWidth 0 0 $destHeight $x $y cm BI /W ${image.width} /H ${image.height} /CS /RGB /F /DCT /BPC 8 ID ")
    writer.flush()
    stream.write(dctImage(image, quality))
    writer.write(" > EI Q")
    writer.flush()

    return stream(
        id,
        stream.toByteArray(),
        properties
    )
}

fun text(
    id: Int,
    text: String,
    x: Int = 0,
    y: Int = 0,
    size: Int = 24,
    properties: PDFValue.PDFDictionary = dict()
): PDFValue.PDFObject {
    return stream(id, "BT /F1 $size Tf $x $y Td ($text)Tj ET", properties)
}