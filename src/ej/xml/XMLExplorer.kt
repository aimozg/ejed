package ej.xml

import ej.editor.utils.readAttributes
import ej.editor.utils.typed
import java.io.InputStream
import java.io.Reader
import javax.xml.stream.XMLEventReader
import javax.xml.stream.XMLInputFactory
import javax.xml.stream.events.*

/*
 * Created by aimozg on 19.07.2018.
 * Confidential until published on GitHub
 */

class XMLExplorer(input: XMLEventReader) {
	constructor(input: InputStream) : this(
			XMLInputFactory.newFactory().createXMLEventReader(input)
	)
	constructor(input: Reader) : this(
			XMLInputFactory.newFactory().createXMLEventReader(input)
	)
	
	private val events = input.typed
	
	fun exploreDocument(handler: XMLExplorer.(String, Map<String, String>) -> Unit) {
		var wasStartDocument = false
		var wasElement = false
		docloop@ for (d in events) when (d) {
			is StartDocument -> wasStartDocument = true
			is StartElement -> {
				if (!wasStartDocument) error("Expected StartDocument, got $d")
				if (!wasElement) {
					wasElement = true
					handler(d.name.localPart, d.readAttributes())
				}
			}
			is EndElement,
			is Characters ->
				if (!wasStartDocument) kotlin.error("Expected StartDocument, got $d")
			is EndDocument -> break@docloop
		}
	}
	
	fun exploreDocument(expectedTag: String, handler: XMLExplorer.(String, Map<String, String>) -> Unit) {
		exploreDocument { tag, _ ->
			if (tag == expectedTag) forEachElement(handler)
			else error("Expected $expectedTag, got $tag")
		}
	}
	
	var skip = 1
	fun forEachElement(handler: XMLExplorer.(String, Map<String, String>) -> Unit) {
		skip--
		loop@ for (e in events) {
			when (e) {
				is StartElement -> {
					if (skip > 0) {
						skip++
					} else {
						skip++
						this.handler(e.name.localPart, e.readAttributes())
					}
				}
				is Characters -> {
				}
				is EndElement -> {
					if (skip > 0) skip--
					else break@loop
				}
				is StartDocument,
				is EndDocument ->
					error("Encountered $e inside an element")
			}
		}
	}
	
	fun text(): String {
		val s = StringBuilder()
		loop@ for (e in events) {
			when (e) {
				is Characters -> s.append(e.data)
				is EndElement -> {
					skip--
					break@loop
				}
				is StartElement,
				is StartDocument,
				is EndDocument ->
					error("Encountered $e inside an element")
			}
		}
		return s.toString()
	}
	
	fun forEachElement(expectedTag: String, handler: XMLExplorer.(Map<String, String>) -> Unit) {
		forEachElement { tag, attrs ->
			if (tag == expectedTag) handler(attrs)
			else error("Expected $expectedTag, got $tag")
		}
	}
}

