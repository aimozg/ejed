package ej.xml

import org.funktionale.either.Either

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
	fun<R> collectElements(handler: XmlExplorerController.(tag:String,attrs:Map<String,String>) -> R): List<R> {
		val list = ArrayList<R>()
		forEachElement { tag, attrs ->
			list.add(handler(tag, attrs))
		}
		return list
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