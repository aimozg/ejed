package ej.editor.utils

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/*
 * Created by aimozg on 27.06.2018.
 * Confidential until published on GitHub
 */

interface CopyAssignable {
//	fun assignFrom(other:T)
//	fun spawnNew():T
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class CopyAssignableIgnore

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class CopyAssignableNoMerge

private fun debug(s:Any?) {
	// println(s)
}

@Suppress("UNCHECKED_CAST")
class CopyAssignableDescriptor<T:CopyAssignable> private constructor(val klass: KClass<T>) {
	fun spawnNew():T = klass.createInstance()
	
	val simpleProperties = ArrayList<KMutableProperty1<T,*>>()
	val caProperties = ArrayList<KMutableProperty1<T,CopyAssignable?>>()
	val listProperties = ArrayList<KProperty1<T,MutableList<*>>>()
	val nomergeProperties = HashSet<KProperty1<T,*>>()
	
	init {
		debug(klass.simpleName)
		for (property in klass.memberProperties) {
			if (property.findAnnotation<CopyAssignableIgnore>() != null) continue
			val type = property.returnType
			if (property is KMutableProperty1) {
				if (type.isSubtypeOf(CopyAssignable::class.starProjectedType.withNullability(true))) {
					debug("\t${property.name} is ca")
					caProperties.add(property as KMutableProperty1<T, CopyAssignable?>)
				} else {
					debug("\t${property.name} is simple")
					simpleProperties.add(property)
				}
			} else if (type.isSubtypeOf(MutableList::class.starProjectedType)) {
				debug("\t${property.name} is list")
				listProperties.add(property as KProperty1<T, MutableList<*>>)
			} else {
				continue
			}
			if (property.findAnnotation<CopyAssignableNoMerge>() != null) {
				debug(", nomerge")
				nomergeProperties.add(property)
			}
		}
	}
	
	companion object {
		private val DESCRIPTORS = HashMap<KClass<*>,CopyAssignableDescriptor<*>>()
		fun<T2:CopyAssignable> find(klass:KClass<out T2>):CopyAssignableDescriptor<T2> {
			return DESCRIPTORS.getOrPut(klass) {
				CopyAssignableDescriptor(klass)
			} as CopyAssignableDescriptor<T2>
		}
		inline fun<reified T2:CopyAssignable> find() = find(T2::class)
	}
}

internal fun<T:CopyAssignable> T.descriptor():CopyAssignableDescriptor<T> =
		CopyAssignableDescriptor.find(this::class)

fun<T:CopyAssignable> T.spawnNew():T = descriptor().spawnNew()

internal fun<T:Any,R:Any?> assignPropertyValue(property: KMutableProperty1<T,R>, dst:T, src:T) {
	property.set(dst, property.get(src))
}
fun<T:CopyAssignable> T.assignFrom(other: T, mergeAll:Boolean = false):T {
	val descriptor = descriptor()
	for (property in descriptor.simpleProperties) {
		if (!mergeAll && property in descriptor.nomergeProperties) continue
		if (property.get(this) == null) {
			assignPropertyValue(property, this, other)
		}
	}
	for (property in descriptor.caProperties) {
		if (!mergeAll && property in descriptor.nomergeProperties) continue
		val oldVal = property.get(this)
		val newVal = property.get(other)
		if (oldVal == null) {
			property.set(this, newVal)
		} else if (newVal != null){
			oldVal.assignFrom(newVal, mergeAll)
		}
	}
	return this
}

fun<T:CopyAssignable> T.spawnCopy():T = spawnNew().assignFrom(this,true)

fun<T:CopyAssignable> mergeAssign(dst:T?, src:T?):T? {
	if (src == null) return dst
	if (dst == null) return src.spawnCopy()
	dst.assignFrom(src)
	return dst
}
fun<T:CopyAssignable> mergeCopy(dst:T?, src:T?):T? = mergeAssign(dst?.spawnCopy(), src)
fun<T:CopyAssignable> mergeAssign(dst:MutableList<T>, src:List<T>): MutableList<T> {
	if (src.isEmpty()) return dst
	dst += src.map { it.spawnCopy() }
	return dst
}
