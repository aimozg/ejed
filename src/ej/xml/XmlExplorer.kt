package ej.xml

import ej.editor.utils.readAttributes
import ej.editor.utils.typed
import org.funktionale.either.Either
import java.io.InputStream
import java.io.Reader
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.*

/*
 * Created by aimozg on 19.07.2018.
 * Confidential until published on GitHub
 */


class XmlExplorer(input: XMLEventReader): XmlExplorerController() {
	constructor(input: InputStream) : this(
			XMLInputFactory.newFactory().createXMLEventReader(input)
	)
	constructor(input: Reader) : this(
			XMLInputFactory.newFactory().createXMLEventReader(input)
	)
	
	private val events = object : Iterator<XMLEvent> {
		val base = input.typed
		override fun hasNext(): Boolean = base.hasNext()
		
		override fun next(): XMLEvent = base.next().also { lastEvent = it }
	}
	private var lastEvent: XMLEvent? = null
	override fun error(msg: String): Nothing {
		val e = lastEvent?.location
		if (e == null) kotlin.error(msg)
		else {
			kotlin.error("At ${e.lineNumber}:${e.columnNumber} $msg")
		}
	}
	
	
	override fun<R> exploreDocument(handler: XmlExplorerController.(rootTag:String, rootAttrs:Map<String, String>) -> R):R {
		var wasStartDocument = false
		var wasElement = false
		var r:R? = null
		docloop@ for (d in events) when (d) {
			is StartDocument -> wasStartDocument = true
			is StartElement -> {
				if (!wasStartDocument) error("Expected StartDocument, got $d")
				if (!wasElement) {
					wasElement = true
					r = handler(d.name.toString(), d.readAttributes())
				}
			}
			is EndElement,
			is Characters ->
				if (!wasStartDocument) kotlin.error("Expected StartDocument, got $d")
			is EndDocument -> break@docloop
		}
		return r ?: error("Malformed document")
	}
	
	var skip = 1
	override fun forEachNode(handler: XmlExplorerController.(Either<String, Pair<String, Map<String, String>>>) -> Unit) {
		skip--
		val text = StringBuilder()
		loop@ for (e in events) {
			when (e) {
				is StartElement -> {
					if (text.isNotEmpty()) {
						this.handler(Either.left(text.toString()))
						text.setLength(0)
					}
					if (skip > 0) {
						skip++
					} else {
						skip++
						this.handler(Either.right(e.name.localPart to e.readAttributes()))
					}
				}
				is Characters -> {
					text.append(e.data)
				}
				is EndElement -> {
					if (text.isNotEmpty()) {
						this.handler(Either.left(text.toString()))
						text.setLength(0)
					}
					if (skip > 0) skip--
					else break@loop
				}
				is StartDocument,
				is EndDocument ->
					error("Encountered $e inside an element")
			}
		}
		if (text.isNotEmpty()) {
			this.handler(Either.left(text.toString()))
			text.setLength(0)
		}
	}
	
}

