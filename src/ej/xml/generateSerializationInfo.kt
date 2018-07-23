package ej.xml

import ej.utils.ifEmpty
import kotlin.coroutines.experimental.buildSequence
import kotlin.reflect.*
import kotlin.reflect.full.*

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Attribute(val name: String = "")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Element(val name: String = "")

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextBody

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Elements(val name: String, val wrapped:Boolean = false, val wrapperName:String="")

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RootElement(val name: String)

interface XmlAutoSerializable : XmlSerializable

private class PropertyTypeinfo(
		val property: KProperty<*>
) {
	val nullable = property.returnType.isMarkedNullable
	val type: KType
	val list: Boolean
	
	init {
		val type0 = property.returnType.withNullability(false)
		if (type0.isSubtypeOf(MutableList::class.starProjectedType)) {
			list = true
			val arg0 = type0.arguments[0]
			if (arg0.variance != KVariance.INVARIANT) error("List element $property must be invariant")
			type = arg0.type ?: error("List element $property must be typed")
		} else {
			list = false
			type = type0
		}
	}
	
	fun textConverter(): TextConverter<*>? =
			when {
				type == String::class.starProjectedType -> StringConverter
				type == Int::class.starProjectedType -> IntConverter
				type == Boolean::class.starProjectedType ->
					if (nullable) NullableBoolConverter else BoolConverter
				type.isSubtypeOf(Enum::class.starProjectedType) -> {
					val to = HashMap<Enum<*>, String>()
					val from = HashMap<String, Enum<*>>()
					@Suppress("UNCHECKED_CAST")
					val klass = type.classifier as KClass<Enum<*>>
					for (enum in klass.java.enumConstants) {
						to[enum] = enum.name
						from[enum.name] = enum
					}
					MappedConverter(to, from)
				}
				else -> null
			}
	
	fun elemConverter(elemName: String) = when {
		type.isSubtypeOf(XmlSerializable::class.starProjectedType) -> {
			val szInfo = (type.classifier as KClass<*>).let { { getSerializationInfo(it) } }
			@Suppress("UNCHECKED_CAST")
			XmlElementConverter(elemName, szInfo as SzInfoMaker<Any>)
		}
		else -> {
			textConverter()?.let {
				TextElementConverter(elemName, it)
			}
		}
	}
}

internal fun <T : XmlAutoSerializable> generateSerializationInfo(clazz: KClass<T>): XmlSerializationInfo<T> = serializationInfo(
		clazz) {
	for (superclass in buildSequence {
		val run = ArrayList(clazz.superclasses)
		while (run.isNotEmpty()) {
			val e = run.removeAt(0)
			if (e.isSubclassOf(XmlSerializable::class) && !e.java.isInterface) {
				@Suppress("UNCHECKED_CAST")
				yield(e as KClass<out XmlSerializable>)
				run.addAll(e.superclasses)
			}
		}
	}) {
		@Suppress("UNCHECKED_CAST")
		val src = getSerializationInfoSafe(superclass) as XmlSerializationInfo<T>?
		if (src != null) copySzInfo(src, this)
	}
	clazz.findAnnotation<RootElement>()?.let { rootAnno ->
		name = rootAnno.name
	}
	for (property in clazz.declaredMemberProperties) {
		val pti by lazy { PropertyTypeinfo(property) }
		for (annotation in property.annotations) when (annotation) {
			is Attribute -> {
				if (pti.list) error("Cannot have @Attribute for list $property")
				val attrName = annotation.name ifEmpty property.name
				if (property !is KMutableProperty1<T, *>) error("@Attribute $property must be mutable for $nameOrClass@$attrName")
				val textConverter = pti.textConverter()
						?: error("@Attribute $property has no compatible to-text converter")
				if (pti.list) error("Cannot @Attribute list $property")
				if (pti.nullable) {
					@Suppress("UNCHECKED_CAST")
					saveAttrN(attrName, property as KMutableProperty1<T, Any?>, textConverter as TextConverter<Any>)
				} else {
					@Suppress("UNCHECKED_CAST")
					saveAttr(attrName, property as KMutableProperty1<T, Any>, textConverter as TextConverter<Any>)
				}
			}
			is Element -> {
				if (pti.list) error("Cannot have @Element for list $property")
				val elemName = annotation.name ifEmpty property.name
				val elemConverter = pti.elemConverter(elemName)
						?: error("@Element $property has no compatible to-element or to-text converter")
				if (property is KMutableProperty1<T,*>) {
					if (pti.nullable) {
						@Suppress("UNCHECKED_CAST")
						registerElementIO(
								NullablePropertyEio(elemConverter as ElementConverter<Any>,
								                    property as KMutableProperty1<T, Any?>),
								elemName
						)
					} else {
						@Suppress("UNCHECKED_CAST")
						registerElementIO(
								PropertyEio(elemConverter as ElementConverter<Any>,
								            property as KMutableProperty1<T, Any>),
								elemName
						)
					}
				} else TODO("elementOverwrite Not implemented yet")
			}
			is Elements -> {
				if (!pti.list) error("Cannot have @Elements for non-list $property")
				val elemName = annotation.name
				val elemConverter = pti.elemConverter(elemName)
						?: error("@Element $property has no compatible to-element or to-text converter")
				if (annotation.wrapped) {
					val wrapperName = annotation.wrapperName ifEmpty property.name
					@Suppress("UNCHECKED_CAST")
					val eio = WrappedListPropertyEio(wrapperName,
					                                 elemConverter as ElementConverter<Any>,
					                                 property as KProperty1<T, MutableList<Any>>)
					registerElementIO(eio,
					                  wrapperName)
				} else {
					@Suppress("UNCHECKED_CAST")
					registerElementIO(ListPropertyEio(elemConverter as ElementConverter<Any>,
					                                  property as KProperty1<T, MutableList<Any>>), elemName)
				}
			}
			is TextBody -> {
				if (pti.list) error("Cannot have @TextBody for list $property")
				if (property !is KMutableProperty1<T, *>) error("@TextBody $property must be mutable for $nameOrClass")
				val textConverter = pti.textConverter()
						?: error("@TextBody $property has no compatible to-text converter")
				if (pti.nullable) {
					@Suppress("UNCHECKED_CAST")
					registerTextIO(NullablePropertyTio(textConverter as TextConverter<Any>,
					                                   property as KMutableProperty1<T, Any?>))
				} else {
					@Suppress("UNCHECKED_CAST")
					registerTextIO(PropertyTio(textConverter as TextConverter<Any>, property as KMutableProperty1<T, Any>))
				}
			}
		}
	}
	if (info.texti == null && info.elements.isEmpty()) emptyBody()
}