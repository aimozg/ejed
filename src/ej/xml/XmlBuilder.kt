package ej.xml

import java.io.OutputStream
import java.io.Writer
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

class XmlBuilder(val output: XMLStreamWriter) {
	private interface Sink<T> {
		operator fun plusAssign(value:T)
	}
	constructor(output: OutputStream) : this(
			XMLOutputFactory.newFactory().createXMLStreamWriter(output)
	)
	
	constructor(output: Writer) : this(
			XMLOutputFactory.newFactory().createXMLStreamWriter(output)
	)
	
	private val events = XMLEventFactory.newFactory()
	fun document(body:XmlBuilder.()->Unit) {
		startDocument()
		body()
		endDocument()
	}
	fun startDocument() {
		output.writeStartDocument()
	}
	fun endDocument() {
		output.writeEndDocument()
		output.close()
	}
	fun element(tag: String, attrs: Map<String, String> = emptyMap(), body: XmlBuilder.() -> Unit) {
		output.writeStartElement(tag)
		for ((k, v) in attrs) output.writeAttribute(k, v)
		body()
		output.writeEndElement()
	}
	fun element(tag:String,body:String="",attrs:Map<String,String> = emptyMap()) {
		if (body.isNotEmpty()) {
			element(tag, attrs) {
				output.writeCharacters(body)
			}
		} else {
			output.writeEmptyElement(tag)
			for ((k, v) in attrs) output.writeAttribute(k, v)
		}
	}
	fun text(data:String) {
		if (data.isNotEmpty()) output.writeCharacters(data)
	}
}