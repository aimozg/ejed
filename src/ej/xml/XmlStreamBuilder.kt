package ej.xml

import java.io.OutputStream
import java.io.Writer
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.XMLStreamWriter

class XmlStreamBuilder(val output: XMLStreamWriter) : XmlBuilder {
	constructor(output: OutputStream) : this(
			XMLOutputFactory.newFactory().createXMLStreamWriter(output)
	)
	
	constructor(output: Writer) : this(
			XMLOutputFactory.newFactory().createXMLStreamWriter(output)
	)
	
	override fun startDocument() {
		output.writeStartDocument()
	}
	
	override fun endDocument() {
		output.writeEndDocument()
		output.close()
	}
	
	override fun emptyElement(tag: String, attrs: Map<String, String>) {
		output.writeEmptyElement(tag)
		for ((k, v) in attrs) output.writeAttribute(k, v)
	}
	
	override fun startElement(tag: String, attrs: Map<String, String>) {
		output.writeStartElement(tag)
		for ((k, v) in attrs) output.writeAttribute(k, v)
	}
	
	override fun endElement() {
		output.writeEndElement()
	}
	
	override fun text(data: String) {
		if (data.isNotEmpty()) output.writeCharacters(data)
	}
}