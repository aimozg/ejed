package ej.editor.utils

import com.sun.org.apache.xerces.internal.parsers.DOMParser
import org.w3c.dom.Document
import org.xml.sax.InputSource
import java.io.InputStream
import java.lang.reflect.Modifier
import javax.xml.stream.XMLEventReader
import javax.xml.stream.events.Attribute
import javax.xml.stream.events.StartElement
import javax.xml.stream.events.XMLEvent

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

val XML_ESCAPABLE_TOKENS = Regex("[&<>]")
val XML_ATTR_ESCAPABLE_TOKENS = Regex("[&<>\"\t\r\n]")
val XML_ENTITIES = mapOf(
		"&" to "&amp;",
		"<" to "&lt;",
		">" to "&gt;",
		"\"" to "&quot;",
		"\t" to "&#x9;",
		"\r" to "&#xD;",
		"\n" to "&#xA;"
)

fun String.escapeXml() = XML_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}
fun String.escapeXmlAttr() = XML_ATTR_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}

val java.lang.reflect.Field.isStatic get() = Modifier.isStatic(modifiers)

fun Pair<String,String>.toXmlAttrString() = " $first=\"${second.escapeXmlAttr()}\""
fun StringBuilder.appendXmlAttrs(vararg attrs:Pair<String,String>) {
	for (attr in attrs) {
		append(attr.toXmlAttrString())
	}
}

fun InputStream.readDocument():Document = DOMParser().let {
	it.parse(InputSource(this))
	it.document
}
@Suppress("UNCHECKED_CAST")
val XMLEventReader.typed get() = (this as Iterator<XMLEvent>)
fun StartElement.readAttributes():Map<String,String> {
	val m = HashMap<String,String>()
	for (attribute in attributes) {
		attribute as Attribute
		m[attribute.name.localPart] = attribute.value
	}
	return m
}
