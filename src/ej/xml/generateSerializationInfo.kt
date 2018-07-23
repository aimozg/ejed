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
annotation class Elements(val name: String)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class WrappedElements(val wrapperName:String, val name: String)

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RootElement(val name: String)

interface XmlAutoSerializable : XmlSerializable

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
		val attrAnno = property.findAnnotation<Attribute>()
		val elemAnno = property.findAnnotation<Element>()
		val elemsAnno = property.findAnnotation<Elements>()
		val welemsAnno = property.findAnnotation<WrappedElements>()
		val textAnno = property.findAnnotation<TextBody>()
		if (attrAnno == null
				&& elemAnno == null
				&& elemsAnno == null
				&& welemsAnno == null
				&& textAnno == null) continue
		val attrName = attrAnno?.name ifEmpty property.name
		val elemName = welemsAnno?.name ifEmpty
				elemAnno?.name ifEmpty
				elemsAnno?.name ifEmpty property.name
		val nullable = property.returnType.isMarkedNullable
		var textConverter: TextConverter<*>? = null
		var elemConverter: ElementConverter<*>? = null
		val list:Boolean
		val type0: KType = property.returnType.withNullability(false)
		val type: KType
		if (type0.isSubtypeOf(MutableList::class.starProjectedType)) {
			list = true
			val arg0 = type0.arguments[0]
			if (arg0.variance != KVariance.INVARIANT) error("List element $property must be invariant")
			type = arg0.type ?: error("List element $property must be typed")
		} else {
			list = false
			type = type0
		}
		when {
			type == String::class.starProjectedType -> textConverter = StringConverter
			type == Int::class.starProjectedType -> textConverter = IntConverter
			type == Boolean::class.starProjectedType ->
				textConverter = if (nullable) NullableBoolConverter else BoolConverter
			type.isSubtypeOf(XmlSerializable::class.starProjectedType) -> {
				val szInfo = (type.classifier as KClass<*>).let { { getSerializationInfo(it) } }
				@Suppress("UNCHECKED_CAST")
				elemConverter = XmlElementConverter(elemName, szInfo as SzInfoMaker<Any>)
			}
			type.isSubtypeOf(Enum::class.starProjectedType) -> {
				val to = HashMap<Enum<*>,String>()
				val from = HashMap<String,Enum<*>>()
				@Suppress("UNCHECKED_CAST")
				val klass = type.classifier as KClass<Enum<*>>
				for (enum in klass.java.enumConstants) {
					to[enum] = enum.name
					from[enum.name] = enum
				}
				textConverter = MappedConverter(to,from)
			}
		}
		if (attrAnno != null) {
			if (property !is KMutableProperty1<T, *>) error("Property @Attribute $property must be mutable for $nameOrClass@$attrName")
			if (textConverter == null) {
				error("Property @Attribute $property has no compatible to-text converter")
			}
			if (list) error("Cannot @Attribute list $property")
			if (nullable) {
				@Suppress("UNCHECKED_CAST")
				saveAttrN(attrName, property as KMutableProperty1<T, Any?>, textConverter as TextConverter<Any>)
			} else {
				@Suppress("UNCHECKED_CAST")
				saveAttr(attrName, property as KMutableProperty1<T, Any>, textConverter as TextConverter<Any>)
			}
		}
		if (elemAnno != null || elemsAnno != null || welemsAnno != null) {
			if (elemConverter == null) {
				if (textConverter != null) {
					elemConverter = TextElementConverter(elemName, textConverter)
				} else {
					error("Property @Element $property has no compatible to-element or to-text converter")
				}
			}
			
			if (list) {
				if (elemAnno != null) error("Cannot have @Element for list $property")
				if (welemsAnno != null) {
					val wrapperName = welemsAnno.wrapperName ifEmpty elemName
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
			} else if (property is KMutableProperty1<T, *>) {
				if (elemsAnno != null) error("Cannot have @Elements for non-list $property")
				if (nullable) {
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
			} else {
				TODO("not implemented yet")
			}
		}
		textAnno?.let {
			if (property !is KMutableProperty1<T, *>) error("Property @Attribute $property must be mutable for $nameOrClass@$attrName")
			if (textConverter == null) {
				error("Property @Attribute $property has no compatible to-text converter")
			}
			if (nullable) {
				@Suppress("UNCHECKED_CAST")
				registerTextIO(NullablePropertyTio(textConverter as TextConverter<Any>,property as KMutableProperty1<T, Any?>))
			} else {
				@Suppress("UNCHECKED_CAST")
				registerTextIO(PropertyTio(textConverter as TextConverter<Any>,property as KMutableProperty1<T, Any>))
			}
		}
	}
	if (info.texti == null && info.elements.isEmpty()) emptyBody()
}