package com.kylecorry.andromeda.xml

data class XMLNode(
    val tag: String,
    val attributes: Map<String, String>,
    val text: String?,
    val children: List<XMLNode>
) {
    companion object {
        fun text(tag: String, text: String?): XMLNode {
            return XMLNode(tag, mapOf(), text, listOf())
        }
    }
}