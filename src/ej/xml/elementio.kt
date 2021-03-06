package ej.xml

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.jvm.isAccessible

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */
interface XmlProducer<in T:Any> {
	fun produce(builder: XmlBuilder, obj: T)
	fun producesText(): Boolean
	fun producesElements(): Boolean
}
interface ElementConsumer<in T: Any> {
	fun consumeElement(obj: T,
	                   tag: String,
	                   attrs: Map<String, String>,
	                   input: XmlExplorerController)
}

abstract class ElementIO<in T : Any> : ElementConsumer<T>, XmlProducer<T>

abstract class AbstractElementIO<in T : Any, A : Any>(
		val converter: ElementConverter<A>
) : ElementIO<T>() {
	abstract fun getValue(obj: T): A?
	abstract fun setValue(obj: T, value: A)
	override fun consumeElement(obj: T,
	                            tag: String,
	                            attrs: Map<String, String>,
	                            input: XmlExplorerController) {
		val a = converter.convert(tag, attrs, input, obj)
		if (a != null) setValue(obj, a)
	}
	
	override fun produce(builder: XmlBuilder, obj: T) {
		val v = getValue(obj)
		if (v != null) converter.write(builder, v)
	}
	
	override fun producesText(): Boolean = converter.producesText()
	override fun producesElements(): Boolean = converter.producesElements()
}

class PropertyEio<in T : Any, A : Any>(
		converter: ElementConverter<A>,
		val prop: KMutableProperty1<in T, A>
) : AbstractElementIO<T, A>(converter) {
	init {
		prop.isAccessible = true
	}
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}

class PropertyOverwritingEio<in T:Any, A:XmlSerializable>(
		val converter: OverwritingElementConverter<A>,
		val prop: KProperty1<in T, A>
) : ElementIO<T>() {
	init {
		prop.isAccessible = true
	}
	override fun consumeElement(obj: T,
	                            tag: String,
	                            attrs: Map<String, String>,
	                            input: XmlExplorerController) {
		val v = prop.get(obj)
		converter.convertInto(v, tag, attrs, input, obj)
	}
	
	override fun produce(builder: XmlBuilder, obj: T) {
		converter.write(builder, prop.get(obj))
	}
	
	override fun producesText(): Boolean = converter.producesText()
	override fun producesElements(): Boolean = converter.producesElements()
}

class NullablePropertyEio<in T : Any, A : Any>(
		converter: ElementConverter<A>,
		val prop: KMutableProperty1<in T, A?>
) : AbstractElementIO<T, A>(converter) {
	init {
		prop.isAccessible = true
	}
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}

class ListPropertyEio<in T : Any, A : Any>(
		val converter: ElementConverter<A>,
		val prop: KProperty1<in T, MutableList<A>>
) : ElementIO<T>() {
	init {
		prop.isAccessible = true
	}
	override fun produce(builder: XmlBuilder, obj: T) {
		for (a in prop.get(obj)) {
			converter.write(builder, a)
		}
	}
	
	override fun producesText(): Boolean = converter.producesText()
	override fun producesElements(): Boolean = converter.producesElements()
	
	override fun consumeElement(obj: T,
	                            tag: String,
	                            attrs: Map<String, String>,
	                            input: XmlExplorerController) {
		val a = converter.convert(tag, attrs, input, obj)
		if (a != null) prop.get(obj) += a
	}
}
class WrappedListPropertyEio<in T: Any, A:Any>(
		val wrapperTag:String,
		val converter: ElementConverter<A>,
		val prop: KProperty1<in T, MutableList<A>>
) : ElementIO<T>() {
	init {
		prop.isAccessible = true
	}
	override fun produce(builder: XmlBuilder, obj: T) {
		val list = prop.get(obj)
		if (list.isNotEmpty()) {
			builder.element(wrapperTag) {
				for (a in list) {
					converter.write(this, a)
				}
			}
		}
	}
	
	override fun producesText(): Boolean = false
	override fun producesElements(): Boolean = true
	
	override fun consumeElement(obj: T,
	                            tag: String,
	                            attrs: Map<String, String>,
	                            input: XmlExplorerController) {
		val list = prop.get(obj)
		input.forEachNode { (l,r) ->
			if (l != null && l.isNotBlank()) error("Unexpected text $l")
			if (r != null) {
				val (etag,eattrs) = r
				val a = converter.convert(etag, eattrs, input, obj)
				if (a != null) list += a
			}
		}
	}
	
}