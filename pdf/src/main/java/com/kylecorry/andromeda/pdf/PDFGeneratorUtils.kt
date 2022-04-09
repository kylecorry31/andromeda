package com.kylecorry.andromeda.pdf

import android.graphics.Bitmap
import java.nio.ByteBuffer

fun catalog(id: String, pages: String): PDFObject {
    return PDFObject(
        id, listOf(
            "/Type /Catalog",
            "/Pages $pages R"
        ), emptyList()
    )
}

fun pages(id: String, children: List<String>): PDFObject {
    return PDFObject(
        id,
        listOf("/Type /Pages", "/Kids [${children.joinToString(" ") { "$it R" }}]"),
        emptyList()
    )
}

fun page(
    id: String,
    parent: String,
    width: Int,
    height: Int,
    contents: List<String>,
    properties: List<String> = emptyList()
): PDFObject {
    return PDFObject(
        id, listOf(
            "/Type /Page",
            "/Parent $parent R",
            "/MediaBox [0 0 $width $height]",
            "/Contents [${contents.joinToString(" ") { "$it R" }}]",
        ) + properties, emptyList()
    )
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

//, listOf("/Resources << /XObject << /X0 4 0 R >> >>")), Add this to the page object properties
//fun imageResource(id: String, image: Bitmap): PDFObject {
//    val size = image.rowBytes * image.height
//    val byteBuffer = ByteBuffer.allocate(size)
//    image.copyPixelsToBuffer(byteBuffer)
//    val byteArray = byteBuffer.array()
//    val stream = ByteArrayOutputStream()
//    image.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//
////    val img = byteArray.joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }
//
//    return stream(
//        id,
//        stream.toByteArray(),
//        listOf(
//            "/Type /XObject",
//            "/Subtype /Image",
//            "/Width ${image.width}",
//            "/Height ${image.height}",
//            "/BitsPerComponent 8",
//            "/Filter /DCTDecode",
//            "/ColorSpace /DeviceRGB",
//            "/DecodeParms << /Quality 100 >>"
//        )
//    )
//}
//
//fun image(
//    id: String,
//    imageName: String,
//    width: Int,
//    height: Int,
//    x: Int = 0,
//    y: Int = 0
//): PDFObject {
//    return stream(id, "Q $width $x 0 $height $y 0 cm $imageName DO Q")
//}

private fun imageToHex(image: Bitmap): String {
    val size = image.rowBytes * image.height
    val byteBuffer = ByteBuffer.allocate(size)
    image.copyPixelsToBuffer(byteBuffer)
    val byteArray = byteBuffer.array().filterIndexed { index, _ -> (index + 1) % 4 != 0 }

    return byteArray.joinToString(separator = " ") { eachByte -> "%02x".format(eachByte) }
}

fun image(
    id: String,
    image: Bitmap,
    x: Int = 0,
    y: Int = 0,
    destWidth: Int = image.width,
    destHeight: Int = image.height,
    properties: List<String> = emptyList()
): PDFObject {
    return stream(
        id,
        "Q $destWidth 0 0 $destHeight $x $y cm BI /W ${image.width} /H ${image.height} /CS /RGB /BPC 8 /F /AHx ID ${
            imageToHex(image)
        } > EI Q",
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