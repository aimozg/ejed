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
	fun forEachElement(expectedTag: String, handler: XmlExplorerController.(attrs:Map<String, String>) -> Unit) {
		forEachElement { tag, attrs ->
			if (tag == expectedTag) handler(attrs)
			else error("Expected $expectedTag, got $tag")
		}
	}
}