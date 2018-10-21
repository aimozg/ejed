package ej.xml

import org.funktionale.either.Either

class XmlObjectExplorer(val root: XmllikeObject) : XmlExplorerController() {
	private var opened = false
	private val queue = ArrayList<XmllikeNode>()
	
	override fun forEachNode(handler: XmlExplorerController.(Either<String, Pair<String, Map<String, String>>>) -> Unit) {
		if (!opened) error("forEachNode() before exploreDocument()")
		
	}
	
	override fun <R> exploreDocument(handler: XmlExplorerController.(rootTag: String, rootAttrs: Map<String, String>) -> R): R {
		if (opened) error("Duplicate exploreDocument()")
		opened = true
		queue.addAll(root.body)
		return handler(root.name, root.attributes)
	}
	
}