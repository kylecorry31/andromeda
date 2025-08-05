package com.kylecorry.andromeda.pdf

fun List<PDFValue.PDFObject>.getByProperty(
    property: String,
    value: PDFValue
): List<PDFValue.PDFObject> {
    return this.filter {
        it.getProperty<PDFValue>(property) == value
    }
}

fun List<PDFValue.PDFObject>.getById(id: Int): PDFValue.PDFObject? {
    return this.firstOrNull { it.id == id }
}

fun PDFValue.PDFObject.getProperties(): PDFValue.PDFDictionary {
    return content.firstOrNull { it is PDFValue.PDFDictionary } as? PDFValue.PDFDictionary
        ?: PDFValue.PDFDictionary(emptyMap())
}

fun PDFValue.PDFDictionary.getProperties(
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