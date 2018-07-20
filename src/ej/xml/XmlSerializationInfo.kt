package ej.xml

import kotlin.reflect.KClass
import kotlin.reflect.jvm.isAccessible

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */
class XmlSerializationInfo<T : Any>(internal val klass: KClass<T>) {
	fun accepts(e:Any) = klass.isInstance(e)
	@Suppress("UNCHECKED_CAST")
	fun serializeIfAccepts(e:Any, tag:String, output:XmlBuilder):Boolean {
		if (accepts(e)) {
			serialize((e as T), tag, output)
			return true
		}
		return false
	}
	internal var name: String? = null
	internal val attri = HashMap<String, AttrConsumer<T>>()
	internal var texti: TextConsumer<T>? = null
	internal val elements = HashMap<String, ElementConsumer<T>>()
	internal val attro = ArrayList<AttrProducer<T>>()
	internal val producers = ArrayList<XmlProducer<T>>()
	internal var beforeSave: (T.()->Unit)? = null
	internal var afterLoad: (T.(Any?)->Unit)? = null
	internal val constructor = klass.constructors.find { it.parameters.isEmpty() }?.apply { isAccessible = true } ?: error("Class $klass has no no-arg constructor")
	internal fun createInstance(): T {
		return constructor.call()
	}
	internal var defaultAttrConsumer = object: AttrConsumer<T> {
		override fun consumeAttr(obj: T, key: String, value: String) {
			error("unknown attribute $klass@$key")
		}
	}
	internal var defaultTextConsumer = object:TextConsumer<T> {
		override fun consumeText(obj: T, data: String) {
			if (data.isNotBlank()) error("unexpected text $klass@$data")
		}
	}
	internal var defaultElementConsumer = object: ElementConsumer<T> {
		override fun consumeElement(obj: T,
		                            tag: String,
		                            attrs: Map<String, String>,
		                            input: XmlExplorerController) {
			error("unexpected element $klass@$tag")
		}
		
	}
	
}

