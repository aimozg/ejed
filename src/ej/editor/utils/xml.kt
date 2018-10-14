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
val XML_KNOWN_ENTITIES by lazy {
	try {
		loadXmlEntities()
	} catch (e: Throwable) {
		System.err.println("Error loading XML Entities")
		e.printStackTrace()
		mapOf(
				"amp" to "&",
				"lt" to "<",
				"gt" to ">",
				"quot" to "\"",
				"apos" to "\'"
		)
	}
}
val XML_ENTITIES = mapOf(
		"&" to "&amp;",
		"<" to "&lt;",
		">" to "&gt;",
		"\"" to "&quot;",
		"\t" to "&#x9;",
		"\r" to "&#xD;",
		"\n" to "&#xA;"
)
val XML_ENTITY_REX = Regex("&(?:([a-zA-Z]+)|#(x[0-9A-Fa-f]+|[0-9]+));")

fun String.escapeXml() = XML_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}
fun String.escapeXmlAttr() = XML_ATTR_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}

fun String.unescapeXml() = XML_ENTITY_REX.replace(this) {
	val name = it.groupValues[1]
	if (name.isNotEmpty()) {
		XML_KNOWN_ENTITIES[name] ?: this.apply {
			System.err.println("Unknown XML entity ${it.value}")
		}
	} else {
		val scode = it.groupValues[2]
		val code = if (scode[0] == 'x') scode.drop(1).toInt(16) else scode.toInt(10)
		code.toChar().toString()
	}
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

private val CSS_SIMPLE_PROP = Regex("""(?<=^|[;/])\s*([a-z\-]+)\s*:\s*([^;]+)(?=[;/]|${'$'})""")
fun extractInlineStyle(styleAttr: String?): Map<String, String> {
	return CSS_SIMPLE_PROP.findAll(styleAttr ?: return emptyMap()).map {
		it.groupValues[1] to it.groupValues[2]
	}.toMap()
}

private val XML_ENTITY_DECL = Regex("""<!ENTITY *([a-zA-Z]+) *"&#(x[0-9A-Fa-f]+|[0-9]+);">""")
private fun loadXmlEntities(): Map<String, String> {
	class Anchor
	return arrayOf("xhtml-lat1.ent", "xhtml-special.ent", "xhtml-symbol.ent").map {
		Anchor::class.java.getResourceAsStream(it).reader().useLines { lines ->
			val d = HashMap<String, String>()
			for (line in lines) {
				for (m in XML_ENTITY_DECL.findAll(line)) {
					val name = m.groupValues[1]
					val scode = m.groupValues[2]
					val code = if (scode[0] == 'x') scode.toInt(16) else scode.toInt()
					d[name] = code.toChar().toString()
				}
			}
			d
		}
	}.foldRight(emptyMap()) { a, b -> a + b }
}
