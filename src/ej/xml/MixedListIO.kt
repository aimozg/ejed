package ej.xml

import kotlin.reflect.KProperty1

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */

open class PolymorphicListIO<T:Any,E:Any>(
		val prop: KProperty1<T, MutableList<E>>,
		val mappings: List<Pair<String,()->XmlSerializationInfo<out E>>>
) : ElementConsumer<T>, XmlProducer<T> {
	protected open fun notfound(e:E,builder: XmlBuilder,obj:T) {
		error("Cannot serialize $e")
	}
	
	override fun produce(builder: XmlBuilder, obj: T) {
		for (e in prop.get(obj)) {
			var found = false
			for ((mtag, szinfo) in mappings) {
				if (szinfo().serializeIfAccepts(e, mtag, builder)) {
					found = true
					break
				}
			}
			if (!found) {
				notfound(e,builder,obj)
			}
		}
	}
	
	override fun consumeElement(obj: T, tag: String, attrs: Map<String, String>, input: XmlExplorerController) {
		val list = prop.get(obj)
		for ((mtag,szinfo) in mappings) {
			if (mtag == tag) {
				list += szinfo().deserialize(input,attrs,obj)
				break
			}
		}
	}
}
class MixedListIO<T:Any,E:Any>(
		prop: KProperty1<T, MutableList<E>>,
		val textConverter: TextConverter<E>,
		mappings: List<Pair<String,()->XmlSerializationInfo<out E>>>
) : TextConsumer<T>, PolymorphicListIO<T,E>(prop,mappings) {
	override fun notfound(e: E, builder: XmlBuilder, obj: T) {
		val s = textConverter.toString(e)
		if (s != null) builder.text(s)
	}
	
	override fun consumeText(obj: T, data: String) {
		prop.get(obj).add(textConverter.convert(data))
	}
	
}