package ej.xml

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1


interface TextConsumer<in T:Any> {
	fun consumeText(obj: T, data: String)
}
abstract class TextProducer<in T:Any>: XmlProducer<T> {
	abstract fun produce(obj: T):String
	override fun produce(builder: XmlBuilder, obj: T) {
		builder.text(produce(obj))
	}
}
abstract class TextIO<in T:Any> : TextConsumer<T>,TextProducer<T>() {

}

abstract class AbstractTio<in T : Any, A : Any>(
		val converter: TextConverter<A>
): TextIO<T>() {
	abstract fun getValue(obj: T): A?
	abstract fun setValue(obj: T, value: A)
	override fun consumeText(obj: T, data: String) {
		val a = converter.convert(data)
		if (a != null) setValue(obj, a)
	}
	
	override fun produce(obj: T): String = converter.toString(getValue(obj))?:""
}
class LambdaTio<in T : Any, A : Any>(
		val getter: (T) -> A?,
		val setter: (T, A) -> Unit,
		converter: TextConverter<A>
) : AbstractTio<T, A>(converter) {
	override fun getValue(obj: T) = getter(obj)
	override fun setValue(obj: T, value: A) = setter(obj, value)
}

class PropertyTio<in T : Any, A : Any>(
		converter: TextConverter<A>,
		val prop: KMutableProperty1<in T, A>
) : AbstractTio<T, A>(converter) {
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}

class NullablePropertyTio<in T : Any, A : Any>(
		converter: TextConverter<A>,
		val prop: KMutableProperty1<in T, A?>
) : AbstractTio<T, A>(converter) {
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}

class ListPropertyTio<in T : Any, A : Any>(
		val converter: TextConverter<A>,
		val prop: KProperty1<in T, MutableList<A>>
) : TextConsumer<T>,TextProducer<T>() {
	override fun produce(obj: T): String = prop.get(obj).joinToString("") {
		converter.toString(it)?:""
	}
	
	override fun consumeText(obj: T, data: String) {
		val a = converter.convert(data)
		if (a != null) prop.get(obj) += a
	}
}