package ej.xml

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

fun <T : Any> XmlSerializationInfo<T>.deserialize(input: XmlExplorerController,
                                                  myAttrs: Map<String, String>,
                                                  parent: Any?): T {
	val obj = createInstance?.invoke()
			?: error("Class $klass has no no-arg constructor")
	deserializeInto(obj,input, myAttrs, parent)
	return obj
}

fun <T : Any> XmlSerializationInfo<T>.deserializeInto(obj: T,
                                                      input: XmlExplorerController,
                                                      myAttrs: Map<String, String>,
                                                      parent: Any?) {
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

fun <T : Any> XmlSerializationInfo<T>.deserializeDocument(input: XmlExplorer): T {
	return input.exploreDocument(name ?: error("$klass XmlSerializationInfo is unnamed")) { attrs ->
		deserialize(input, attrs, null)
	}
}

fun <T : Any> XmlSerializationInfo<T>.serialize(
		obj: T,
		tag: String,
		output: XmlBuilder,
		attrModifier: (Map<String,String>)->Map<String,String> ={it}
) {
	beforeSave?.invoke(obj)
	output.element(tag,
	               attrModifier(attro.mapNotNull { it.produce(obj) }.toMap())
	) {
		for (producer in producers) {
			producer.produce(this, obj)
		}
	}
	afterSave?.invoke(obj)
}

fun <T : Any> XmlSerializationInfo<T>.serializeDocument(obj: T, output: XmlBuilder) {
	output.startDocument()
	serialize(obj, name ?: error("$obj serialization info has no name"), output)
	output.endDocument()
}