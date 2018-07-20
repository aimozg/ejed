package ej.xml

import java.io.OutputStream
import java.io.Writer
import javax.xml.stream.XMLEventFactory
import javax.xml.stream.XMLEventWriter
import javax.xml.stream.XMLOutputFactory
import javax.xml.stream.events.XMLEvent

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

class XmlBuilder(val output: XMLEventWriter) {
	private interface Sink<T> {
		operator fun plusAssign(value:T)
	}
	constructor(output: OutputStream) : this(
			XMLOutputFactory.newFactory().createXMLEventWriter(output)
	)
	
	constructor(output: Writer) : this(
			XMLOutputFactory.newFactory().createXMLEventWriter(output)
	)
	
	private val events = XMLEventFactory.newFactory()
	private val out = object:Sink<XMLEvent> {
		override fun plusAssign(value: XMLEvent) {
			output.add(value)
		}
	}
	fun document(body:XmlBuilder.()->Unit) {
		startDocument()
		body()
		endDocument()
	}
	fun startDocument() {
		out += events.createStartDocument()
	}
	fun endDocument() {
		out += events.createEndDocument()
		output.close()
	}
	fun element(tag: String, attrs: Map<String, String> = emptyMap(), body: XmlBuilder.() -> Unit) {
		out += events.createStartElement(
				"",
				"",
				tag,
				attrs.map { (k, v) ->
					events.createAttribute(k, v)
				}.iterator(),
				emptyList<Any?>().iterator()
		)
		body()
		out += events.createEndElement("","",tag)
	}
	fun element(tag:String,body:String="",attrs:Map<String,String> = emptyMap()) {
		element(tag,attrs) {
			if (body.isNotEmpty()) out += events.createCharacters(body)
		}
	}
	fun text(data:String) {
		if (data.isNotEmpty()) out += events.createCharacters(data)
	}
}