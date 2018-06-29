package ej.editor.utils

import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.*

/*
 * Created by aimozg on 27.06.2018.
 * Confidential until published on GitHub
 */

interface Patchable {
//	fun spawnNew():ThisType
//  fun spawnCopy():ThisType
//	fun applyPatch(other:ThisType):this
//	fun cop
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class PatchIgnore

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.PROPERTY)
annotation class PatchNoMerge

private fun debug(s:Any?) {
	// println(s)
}

@Suppress("UNCHECKED_CAST")
class PatchDescriptor<T:Patchable> private constructor(val klass: KClass<T>) {
	fun spawnNew():T = klass.createInstance()
	
	val simpleProperties = ArrayList<KMutableProperty1<T,*>>()
	val patchableProperties = ArrayList<KMutableProperty1<T,Patchable?>>()
	val listProperties = ArrayList<KProperty1<T,MutableList<*>>>()
	val nomergeProperties = HashSet<KProperty1<T,*>>()
	
	init {
		debug(klass.simpleName)
		for (property in klass.memberProperties) {
			if (property.findAnnotation<PatchIgnore>() != null) continue
			val type = property.returnType
			if (property is KMutableProperty1) {
				if (type.isSubtypeOf(Patchable::class.starProjectedType.withNullability(true))) {
					debug("\t${property.name} is ca")
					patchableProperties.add(property as KMutableProperty1<T, Patchable?>)
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
			if (property.findAnnotation<PatchNoMerge>() != null) {
				debug(", nomerge")
				nomergeProperties.add(property)
			}
		}
	}
	
	companion object {
		private val DESCRIPTORS = HashMap<KClass<*>,PatchDescriptor<*>>()
		fun<T2:Patchable> find(klass:KClass<out T2>):PatchDescriptor<T2> {
			return DESCRIPTORS.getOrPut(klass) {
				PatchDescriptor(klass)
			} as PatchDescriptor<T2>
		}
		inline fun<reified T2:Patchable> find() = find(T2::class)
	}
}

internal fun<T:Patchable> T.descriptor():PatchDescriptor<T> =
		PatchDescriptor.find(this::class)

fun<T:Patchable> T.spawnNew():T = descriptor().spawnNew()

fun<T:Patchable> T?.sameValuesAs(other:T?):Boolean {
	if (this == null) return other == null
	if (other == null) return false
	val descriptor = this.descriptor()
	return descriptor.simpleProperties.none { prop ->
		prop.get(this) != prop.get(other)
	} && descriptor.patchableProperties.none { prop ->
		!prop.get(this).sameValuesAs(prop.get(other))
	}
}
internal fun<T:Any,R:Any?> assignPropertyValue(property: KMutableProperty1<T,R>, dst:T, src:T) {
	property.set(dst, property.get(src))
}
fun<T:Patchable> T.applyPatch(other: T?, mergeAll:Boolean = false):T {
	if (other == null) return this
	val descriptor = descriptor()
	for (property in descriptor.simpleProperties) {
		if (!mergeAll && property in descriptor.nomergeProperties) continue
		if (property.get(this) == null) {
			assignPropertyValue(property, this, other)
		}
	}
	for (property in descriptor.patchableProperties) {
		if (!mergeAll && property in descriptor.nomergeProperties) continue
		val oldVal = property.get(this)
		val newVal = property.get(other)
		if (oldVal == null) {
			property.set(this, newVal?.spawnCopy())
		} else if (newVal != null){
			oldVal.applyPatch(newVal, mergeAll)
		}
	}
	return this
}
internal fun<T:Any,R:Patchable> assignPatchedPropertyValue(property: KMutableProperty1<T,R?>, dst:T, oldVal:R): R? {
	val patch = property.get(dst)?.toPatch(oldVal)
	property.set(dst, patch)
	return patch
}
@Suppress("UNCHECKED_CAST")
fun <T:Patchable> T.toPatch(older: T, includeAll:Boolean = false):T? {
	val descriptor = descriptor()
	var modified = false
	for (property in descriptor.simpleProperties) {
		if (!includeAll && property in descriptor.nomergeProperties) continue
		val newVal = property.get(this)
		val oldVal = property.get(older)
		if (newVal == oldVal) {
			(property as KMutableProperty1<T,Any?>).set(this, null)
		} else {
			modified = true
		}
	}
	for (property in descriptor.patchableProperties) {
		if (!includeAll && property in descriptor.nomergeProperties) continue
		val newVal = property.get(this)
		val oldVal = property.get(older)
		if (oldVal != null && newVal != null) {
			val newValUpd = assignPatchedPropertyValue(property, this, oldVal)
			if (newValUpd != null) {
				modified = true
			}
		}
	}
	return if (modified) this else null
}

fun<T:Patchable> T.spawnCopy():T = spawnNew().applyPatch(this, true)

fun<T:Patchable> spawnPatchedCopy(dst:T, src:T?):T = dst.spawnCopy().applyPatch(src)
fun<T:Patchable> MutableList<T>.applyPatch( src:List<T>): MutableList<T> {
	if (src.isEmpty()) return this
	this += src.map { it.spawnCopy() }
	return this
}
