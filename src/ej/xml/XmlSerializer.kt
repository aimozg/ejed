package ej.xml

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

fun <T : Any> XmlSerializationInfo<T>.deserialize(input: XmlExplorerController,
                                                  myAttrs: Map<String, String>,
                                                  parent: Any?): T {
	val obj = createInstance()
	for ((k, v) in myAttrs) {
		val aio = attri[k] ?: defaultAttrConsumer
		aio.consumeAttr(obj, k, v)
	}
	val textbody = texti ?: defaultTextConsumer
	input.forEachNode { (l, r) ->
		if (l != null) {
			textbody.consumeText(obj, l)
		}
		if (r != null) {
			val (tag, attrs) = r
			val eio = elements[tag] ?: defaultElementConsumer
			eio.consumeElement(obj, tag, attrs, input)
		}
	}
	afterLoad?.invoke(obj, parent)
	return obj
}

fun <T : Any> XmlSerializationInfo<T>.deserializeDocument(input: XmlExplorer): T {
	return input.exploreDocument(name ?: error("Cannot load as document unnamed XmlSerializationInfo")) { attrs ->
		deserialize(input, attrs, null)
	}
}

fun <T : Any> XmlSerializationInfo<T>.serialize(obj: T, tag: String, output: XmlBuilder) {
	beforeSave?.invoke(obj)
	output.element(tag,
	               attro.mapNotNull { it.produce(obj) }.toMap()
	) {
		for (producer in producers) {
			producer.produce(this, obj)
		}
	}
}
fun <T:Any> XmlSerializationInfo<T>.serializeDocument(obj: T, output: XmlBuilder) {
	output.startDocument()
	serialize(obj,name?:error("$obj serialization info has no name"), output)
	output.endDocument()
}