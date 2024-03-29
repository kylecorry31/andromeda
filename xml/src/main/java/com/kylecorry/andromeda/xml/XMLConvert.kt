package com.kylecorry.andromeda.xml

import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter

object XMLConvert {

    fun toString(xml: XMLNode, isRoot: Boolean = true): String {
        return ByteArrayOutputStream().use { stream ->
            write(xml, stream, isRoot)
            stream.toString()
        }
    }

    fun write(xml: XMLNode, stream: OutputStream, isRoot: Boolean = true) {
        stream.bufferedWriter().use { writer ->
            write(xml, writer, isRoot)
        }
    }

    private fun write(xml: XMLNode, writer: BufferedWriter, isRoot: Boolean = true) {
        if (isRoot) {
            writer.write("<?xml version=\"1.0\"?>")
        }
        val attributes = if (xml.attributes.isEmpty()) "" else " " + xml.attributes.toList()
            .joinToString(" ") { "${it.first}=\"${it.second}\"" }
        writer.write("<${xml.tag}")
        writer.write(attributes)
        writer.write(">")
        xml.text?.let {
            writer.write(it)
        }
        xml.children.forEach {
            write(it, writer, false)
        }
        writer.write("</${xml.tag}>")
    }


    fun parse(xml: String): XMLNode {
        val inputStream = xml.byteInputStream()
        return parse(inputStream, true)
    }

    fun parse(stream: InputStream, autoClose: Boolean = false): XMLNode {
        try {
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(stream, null)
            parser.nextTag()
            return getXMLTree(parser)
        } finally {
            if (autoClose) {
                stream.close()
            }
        }
    }

    private fun getXMLTree(parser: XmlPullParser): XMLNode {
        val root = parser.name
        val attributes = mutableMapOf<String, String>()
        for (i in 0 until parser.attributeCount) {
            attributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        }
        var text: String? = null
        val children = mutableListOf<XMLNode>()
        while (parser.next() != XmlPullParser.END_DOCUMENT) {
            if (parser.eventType == XmlPullParser.TEXT) {
                text = parser.text
                if (text.isNullOrBlank()) {
                    text = null
                }
            }
            if (parser.eventType == XmlPullParser.START_TAG) {
                children.add(getXMLTree(parser, parser.name))
            }
        }
        return XMLNode(root, attributes, text, children)
    }


    private fun getXMLTree(parser: XmlPullParser, tag: String): XMLNode {
        val children = mutableListOf<XMLNode>()
        var text: String? = null
        var attributes = mutableMapOf<String, String>()
        for (i in 0 until parser.attributeCount) {
            attributes[parser.getAttributeName(i)] = parser.getAttributeValue(i)
        }
        while (!(parser.next() == XmlPullParser.END_TAG && parser.name == tag)) {
            if (parser.eventType == XmlPullParser.TEXT) {
                text = parser.text
                if (text.isNullOrBlank()) {
                    text = null
                }
            }
            if (parser.eventType == XmlPullParser.START_TAG) {
                children.add(getXMLTree(parser, parser.name))
            }
        }
        return XMLNode(tag, attributes, text, children)
    }

}