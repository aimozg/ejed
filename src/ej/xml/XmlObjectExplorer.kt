package ej.xml

import ej.utils.*
import org.funktionale.either.Either

class XmlObjectExplorer(val root: XmllikeObject) : XmlExplorerController() {
	private var opened = false
	private val queue = ArrayList<List<XmllikeNode>>()
	
	override fun forEachNode(handler: XmlExplorerController.(Either<String, Pair<String, Map<String, String>>>) -> Unit) {
		if (!opened) error("forEachNode() before exploreDocument()")
		if (queue.isEmpty()) return
		queue.peek().forEach {
			it.fold(
					{ text -> this.handler(text.iAmEitherLeft()) },
					{ obj ->
						queue.push(obj.body)
						handler.invoke(this, (obj.name to obj.attributes))
						queue.pop()
					}
			)
		}
	}
	
	override fun <R> exploreDocument(handler: XmlExplorerController.(rootTag: String, rootAttrs: Map<String, String>) -> R): R {
		if (opened) error("Duplicate exploreDocument()")
		opened = true
		queue.push(root.body)
		return handler(root.name, root.attributes)
	}
	
}