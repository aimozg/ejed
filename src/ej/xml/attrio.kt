package ej.xml

import kotlin.reflect.KMutableProperty1
import kotlin.reflect.jvm.isAccessible

/*
 * Created by aimozg on 20.07.2018.
 * Confidential until published on GitHub
 */
interface AttrConsumer<in T:Any> {
	fun consumeAttr(obj: T, key: String, value: String)
}
abstract class AttrProducer<in T:Any> {
	abstract fun produce(obj: T): Pair<String,String>?
}
abstract class AttrIO<in T:Any> : AttrConsumer<T>,AttrProducer<T>(){
}
abstract class AbstractAio<in T : Any, A : Any>(
		val name:String,
		val converter: TextConverter<A>
): AttrIO<T>() {
	abstract fun getValue(obj: T): A?
	abstract fun setValue(obj: T, value: A)
	override fun consumeAttr(obj: T, key: String, value: String) {
		val a = converter.convert(value)
		if (a != null) setValue(obj, a)
	}
	override fun produce(obj: T): Pair<String, String>? {
		val v = converter.toString(getValue(obj))
		return if (v == null) null else name to v
	}
}
class LambdaAio<in T : Any, A : Any>(
		name:String,
		val getter: (T) -> A?,
		val setter: (T, A) -> Unit,
		converter: TextConverter<A>
) : AbstractAio<T, A>(name,converter) {
	override fun getValue(obj: T) = getter(obj)
	override fun setValue(obj: T, value: A) = setter(obj, value)
}

class PropertyAio<in T : Any, A : Any>(
		name:String,
		converter: TextConverter<A>,
		val prop: KMutableProperty1<in T, A>
) : AbstractAio<T, A>(name,converter) {
	init {
		prop.isAccessible = true
	}
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}

class NullablePropertyAio<in T : Any, A : Any>(
		name:String,
		converter: TextConverter<A>,
		val prop: KMutableProperty1<in T, A?>
) : AbstractAio<T, A>(name,converter) {
	init {
		prop.isAccessible = true
	}
	override fun getValue(obj: T): A? = prop.get(obj)
	override fun setValue(obj: T, value: A) = prop.set(obj, value)
}
