package ej.xml

import org.funktionale.either.Either
import java.util.*

abstract class XmlExplorerController {
	abstract fun forEachNode(handler: XmlExplorerController.(Either<String, Pair<String, Map<String, String>>>)->Unit)

	fun text(): String {
		val s = StringBuilder()
		forEachNode { (l, r) ->
			if (l != null) s.append(l)
			else if (r != null) error("Expected text only, got <${r.first}>")
		}
		return s.toString()
	}
	fun forEachElement(handler: XmlExplorerController.(tag:String, attrs:Map<String, String>) -> Unit) {
		forEachNode { (_, r) ->
			if (r != null) handler(r.first, r.second)
		}
	}
	fun<R> collectNodes(handler: XmlExplorerController.(Either<String, Pair<String, Map<String, String>>>)->R):List<R> {
		val list = ArrayList<R>()
		forEachNode {
			list.add(handler(it))
		}
		return list
	}
	fun<R> collectElements(handler: XmlExplorerController.(tag:String,attrs:Map<String,String>) -> R): List<R> {
		val list = ArrayList<R>()
		forEachElement { tag, attrs ->
			list.add(handler(tag, attrs))
		}
		return list
	}
	fun<R> collectOneElement(handler: XmlExplorerController.(tag:String,attrs:Map<String,String>)->R):R {
		var result:Optional<R> = Optional.empty()
		forEachElement { tag, attrs ->
			if (result.isPresent) error("Expected only one element, got $tag")
			result = Optional.of(handler(tag,attrs))
		}
		return result.orElseThrow { IllegalStateException("Expected element, got nothing") }
	}
	fun forEachElement(expectedTag: String, handler: XmlExplorerController.(attrs:Map<String, String>) -> Unit) {
		forEachElement { tag, attrs ->
			if (tag == expectedTag) handler(attrs)
			else error("Expected $expectedTag, got $tag")
		}
	}
	
	abstract fun<R> exploreDocument(handler: XmlExplorerController.(rootTag: String, rootAttrs: Map<String, String>) -> R): R
	
	fun<R> exploreDocument(expectedTag: String, handler: XmlExplorerController.(rootAttrs: Map<String, String>) -> R): R {
		return exploreDocument { tag, attrs ->
			if (tag == expectedTag) handler(attrs)
			else error("Expected $expectedTag, got $tag")
		}
	}
	
	fun exploreDocumentThenElements(expectedTag: String, handler: XmlExplorerController.(tag: String, attrs: Map<String, String>) -> Unit) {
		exploreDocument { tag, _ ->
			if (tag == expectedTag) forEachElement(handler)
			else error("Expected $expectedTag, got $tag")
		}
	}
}