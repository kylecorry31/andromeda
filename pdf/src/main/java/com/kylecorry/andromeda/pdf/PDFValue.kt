package com.kylecorry.andromeda.pdf

sealed interface PDFValue {

    fun toByteArray(): ByteArray {
        return toString().toByteArray()
    }

    class PDFObject(val id: Int, val generation: Int = 0, val content: List<PDFValue>) : PDFValue {

        val reference: PDFIndirectObject
            get() = PDFIndirectObject(id, generation)

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFObject) return false
            return id == other.id && generation == other.generation && content == other.content
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + generation
            result = 31 * result + content.hashCode()
            return result
        }

        fun hasProperty(key: PDFName): Boolean {
            return content.any { it is PDFDictionary && it.contains(key) }
        }

        fun hasProperty(key: String): Boolean {
            return content.any { it is PDFDictionary && it.contains(key) }
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : PDFValue> getProperty(key: PDFName): T? {
            return content.firstOrNull { it is PDFDictionary }?.let { it as PDFDictionary }
                ?.get(key) as? T
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : PDFValue> getProperty(key: String): T? {
            return content.firstOrNull { it is PDFDictionary }?.let { it as PDFDictionary }
                ?.get(key) as? T
        }

        override fun toString(): String {
            return "$id $generation obj\n" +
                    content.joinToString(separator = "\n") +
                    "\nendobj"
        }
    }

    class PDFStream(val content: ByteArray) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFStream) return false
            return content.contentEquals(other.content)
        }

        override fun hashCode(): Int {
            return content.contentHashCode()
        }

        override fun toString(): String {
            return "stream\n${content.decodeToString()}\nendstream"
        }

        override fun toByteArray(): ByteArray {
            return "stream\n".toByteArray() + content + "\nendstream".toByteArray()
        }
    }

    class PDFName(_name: String) : PDFValue {
        val name = (if (_name.startsWith("/")) _name else "/$_name")

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFName) return false
            return name.lowercase() == other.name.lowercase()
        }

        override fun hashCode(): Int {
            return name.lowercase().hashCode()
        }

        override fun toString(): String {
            return name
        }
    }

    class PDFDictionary(val properties: Map<PDFName, PDFValue>) : PDFValue {
        operator fun get(key: PDFName): PDFValue? {
            return properties[key]
        }

        operator fun get(key: String): PDFValue? {
            return properties[PDFName(key)]
        }

        fun hasValue(key: PDFName, value: PDFValue): Boolean {
            return properties[key] == value
        }

        fun hasValue(key: String, value: PDFValue): Boolean {
            return properties[PDFName(key)] == value
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : PDFValue> getAs(key: PDFName): T? {
            return properties[key] as? T
        }

        @Suppress("UNCHECKED_CAST")
        fun <T : PDFValue> getAs(key: String): T? {
            return properties[PDFName(key)] as? T
        }

        fun contains(key: PDFName): Boolean {
            return properties.containsKey(key)
        }

        fun contains(key: String): Boolean {
            return properties.containsKey(PDFName(key))
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFDictionary) return false
            return properties == other.properties
        }

        override fun hashCode(): Int {
            return properties.hashCode()
        }

        override fun toString(): String {
            return properties.entries.joinToString(
                prefix = "<<",
                postfix = ">>",
                separator = " "
            ) { "${it.key} ${it.value}" }
        }
    }

    class PDFString(val value: String) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFString) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return "($value)"
        }
    }

    class PDFNumber(val value: Number) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFNumber) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    class PDFBoolean(val value: Boolean) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFBoolean) return false
            return value == other.value
        }

        override fun hashCode(): Int {
            return value.hashCode()
        }

        override fun toString(): String {
            return value.toString()
        }
    }

    class PDFArray(val values: List<PDFValue>) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFArray) return false
            return values == other.values
        }

        override fun hashCode(): Int {
            return values.hashCode()
        }

        override fun toString(): String {
            return values.joinToString(prefix = "[", postfix = "]", separator = " ")
        }
    }

    class PDFNull : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFNull) return false
            return true
        }

        override fun hashCode(): Int {
            return 0
        }

        override fun toString(): String {
            return "null"
        }
    }

    class PDFIndirectObject(
        val id: Int,
        val generation: Int = 0,
    ) : PDFValue {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is PDFIndirectObject) return false
            return id == other.id && generation == other.generation
        }

        override fun hashCode(): Int {
            var result = id
            result = 31 * result + generation
            return result
        }

        override fun toString(): String {
            return "$id $generation R"
        }
    }
}