package ej.xml

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

fun <T : Any> XmlSerializationInfo<T>.deserializeInto(obj: T,
                                                      input: XmlExplorerController,
                                                      myAttrs: Map<String, String>,
                                                      parent: Any?) {
	beforeLoad?.invoke(obj, parent)
	for ((k, v) in myAttrs) {
		val aio = attri[k] ?: defaultAttrConsumer
		aio.consumeAttr(obj, k, v, input)
	}
	val textbody = texti ?: defaultTextConsumer
	input.forEachNode { (l, r) ->
		if (l != null) {
			textbody.consumeText(obj, l, input)
		}
		if (r != null) {
			val (tag, attrs) = r
			deserializeElementInto(obj, input, tag, attrs)
		}
	}
	afterLoad?.invoke(obj, parent)
}

fun <T: Any> XmlSerializationInfo<T>.deserializeElementInto(obj: T,
                                                            input: XmlExplorerController,
                                                            tag: String,
                                                            attrs: Map<String, String>) {
	
	val eio = elements[tag] ?: defaultElementConsumer
	eio.consumeElement(obj, tag, attrs, input)
}

fun <T : Any> XmlSerializationInfo<T>.deserializeDocument(input: XmlExplorerController): T {
	return input.exploreDocument(name ?: error("$klass XmlSerializationInfo is unnamed")) { attrs ->
		deserialize(input, attrs, null)
	}
}

fun <T : Any> AXmlSerializationInfo<T>.serializeDocument(obj: T, nameOverride: String, output: XmlBuilder) {
	output.startDocument()
	serialize(obj, nameOverride, output)
	output.endDocument()
}

fun <T : Any> AXmlSerializationInfo<T>.serializeDocument(obj: T, output: XmlBuilder) {
	serializeDocument(obj, name ?: error("$obj serialization info has no name"), output)
}
