package com.kylecorry.andromeda.pdf

import android.graphics.Bitmap
import com.kylecorry.sol.math.Vector2
import com.kylecorry.sol.units.Coordinate
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

fun catalog(id: String, pages: String): PDFObject {
    return PDFObject(
        id, listOf(
            type("Catalog"),
            "/Pages ${ref(pages)}"
        ), emptyList()
    )
}

fun pages(id: String, children: List<String>): PDFObject {
    return PDFObject(
        id,
        listOf(
            type("Pages"),
            "/Kids ${array(children) { ref(it) }}"
        ),
        emptyList()
    )
}

fun page(
    id: String,
    parent: String,
    width: Int,
    height: Int,
    contents: List<String>,
    viewportIds: List<String> = emptyList(),
    properties: List<String> = emptyList()
): PDFObject {
    return PDFObject(
        id, listOfNotNull(
            type("Page"),
            "/Parent ${ref(parent)}",
            "/MediaBox [0 0 $width $height]",
            "/Contents ${array(contents) { ref(it) }}",
            if (viewportIds.isEmpty()) null else "/VP ${array(viewportIds) { ref(it) }}"
        ) + properties, emptyList()
    )
}

fun viewport(
    id: String,
    measureId: String,
    bbox: DoubleArray,
    properties: List<String> = emptyList()
): PDFObject {
    return PDFObject(
        id, listOf(
            type("Viewport"),
            "/Measure ${ref(measureId)}",
            "/BBox ${array(bbox.toTypedArray())}"
        ) + properties, emptyList()
    )
}

fun geo(
    id: String,
    gpts: List<Coordinate>,
    lpts: List<Vector2> = listOf(
        Vector2(0f, 1f),
        Vector2(0f, 0f),
        Vector2(1f, 0f),
        Vector2(1f, 1f)
    ),
    gcsId: String? = null,
    bounds: List<Vector2> = listOf(
        Vector2(0f, 1f),
        Vector2(0f, 0f),
        Vector2(1f, 0f),
        Vector2(1f, 1f)
    ),
    properties: List<String> = emptyList()
): PDFObject {
    return PDFObject(
        id, listOfNotNull(
            type("Measure"),
            subtype("GEO"),
            "/Bounds ${array(bounds.toTypedArray()) { "${it.x} ${it.y}" }}",
            "/LPTS ${array(lpts.toTypedArray()) { "${it.x} ${it.y}" }}",
            "/GPTS ${array(gpts.toTypedArray()) { "${it.latitude} ${it.longitude}" }}",
            gcsId?.let { "/GCS ${ref(it)}" }
        ) + properties, emptyList()
    )
}

fun gcs(
    id: String,
    projcs: ProjectedCoordinateSystem,
    properties: List<String> = emptyList()
): PDFObject {
    val datum = projcs.geographic.datum
    val spheroid = datum.spheroid

    return PDFObject(
        id, listOf(
            type("PROJCS"),
            "/WKT (PROJCS[\"WGS 84\",GEOGCS[\"WGS 84\",DATUM[\"${datum.name}\",SPHEROID[\"${spheroid.name}\",${spheroid.semiMajorAxis},${spheroid.inverseFlattening}]]],PROJECTION[\"${projcs.projection}\"]])"
        ) + properties, emptyList()
    )
}

fun type(type: String): String {
    return "/Type ${name(type)}"
}

fun subtype(subtype: String): String {
    return "/Subtype ${name(subtype)}"
}

fun name(name: String): String {
    return if (name.startsWith("/")) name else "/$name"
}

fun ref(id: String): String {
    return "$id R"
}

fun <T> array(arr: List<T>, toString: (T) -> String = { it.toString() }): String {
    return "[${arr.joinToString(" ") { toString(it) }}]"
}

fun <T> array(arr: Array<T>, toString: (T) -> String = { it.toString() }): String {
    return "[${arr.joinToString(" ") { toString(it) }}]"
}

fun bbox(left: Number, top: Number, right: Number, bottom: Number): DoubleArray {
    return doubleArrayOf(left.toDouble(), top.toDouble(), right.toDouble(), bottom.toDouble())
}

fun stream(id: String, contents: String, properties: List<String> = emptyList()): PDFObject {
    return stream(id, contents.toByteArray(), properties)
}

fun stream(id: String, contents: ByteArray, properties: List<String> = emptyList()): PDFObject {
    return PDFObject(
        id, listOf(
            "/Length ${contents.size}"
        ) + properties, listOf(contents)
    )
}

private fun dctImage(image: Bitmap, quality: Int = 100): ByteArray {
    val stream = ByteArrayOutputStream()
    image.compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

fun image(
    id: String,
    image: Bitmap,
    x: Int = 0,
    y: Int = 0,
    destWidth: Int = image.width,
    destHeight: Int = image.height,
    quality: Int = 100,
    properties: List<String> = emptyList()
): PDFObject {

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
    id: String,
    text: String,
    x: Int = 0,
    y: Int = 0,
    size: Int = 24,
    properties: List<String> = emptyList()
): PDFObject {
    return stream(id, "BT /F1 $size Tf $x $y Td ($text)Tj ET", properties)
}