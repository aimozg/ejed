package ej.xml

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

interface XmlBuilder {
	fun startDocument()
	fun endDocument()
	fun startElement(tag: String, attrs: Map<String, String> = emptyMap())
	fun endElement()
	fun text(data: String)
	fun emptyElement(tag: String, attrs: Map<String, String> = emptyMap()) {
		startElement(tag, attrs)
		endElement()
	}
}

fun <T : XmlBuilder> T.document(body: T.() -> Unit) {
	startDocument()
	body()
	endDocument()
}

fun <T : XmlBuilder> T.element(tag: String, attrs: Map<String, String> = emptyMap(), body: T.() -> Unit) {
	startElement(tag, attrs)
	body()
	endElement()
}

fun XmlBuilder.element(tag: String, body: String = "", attrs: Map<String, String> = emptyMap()) {
	if (body.isEmpty()) {
		emptyElement(tag, attrs)
	} else {
		startElement(tag, attrs)
		text(body)
		endElement()
	}
}
