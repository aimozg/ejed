package ej.xml

import ej.utils.ifEmpty
import org.funktionale.either.Either
import kotlin.coroutines.experimental.buildSequence
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

/**
 * ```
 * @Attribute var name: String = "value";
 * @Attribute("v2") var name2: String = "value2";
 * ```
 * <->
 * ```<element... name="value" v2="value2" .../>```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Attribute(val name: String = "")

/**
 * ```
 * @Element var name: String = "value";
 * @Element("v2") var name2: String = "value2";
 * ```
 * <->
 * ```<name>value</name> <v2>value2</v2>```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Element(val name: String = "")

/**
 * ```
 * @TextBody var name: String = "value";
 * ```
 * <->
 * ```<owner-element... >value</owner-element>```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class TextBody

/**
 * ```
 * @Elements("item") val items = arrayListOf("me", "her")
 * @Elements("person", true) val persons = arrayListOf("Alice", "Bob")
 * @Elements("object")
 * val objects = arrayListOf(XmlAutoSerializableA, XmlAutoSerializableB)
 * ```
 * <->
 * ```
 * <item>me</item>
 * <item>her</item>
 * <persons>
 *     <person>Alice</person>
 *     <person>Bob</person>
 * </persons>
 * <object some-property="value">
 *     <some-other-property>A</some-other-property>
 * </object>
 * <object some-property="value2">
 *     <some-other-property>B</some-other-property>
 * </object>
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Elements(val name: String, val wrapped:Boolean = false, val wrapperName:String="")

/**
 * ```
 * @PolymorphicElements(polymorphisms=[
 *      Polymorphism("gun", Gun::class),
 *      Polymorphism("sword", Sword::class)
 * ])
 * val items = arrayListOf(Sword("Excalibur"), Gun("Railgun"))
 * ```
 * <->
 * ```
 * <sword>Excalibur</sword>
 * <gun>Railgun</gun>
 * ```
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class PolymorphicElements(
		val wrapped:Boolean = false,
		val wrapperName: String = "",
		vararg val polymorphisms:Polymorphism
)
@Retention(AnnotationRetention.RUNTIME)
annotation class Polymorphism(val qualifier:String,val klass:KClass<out XmlSerializable>)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MixedBody(
		val stringConverter:KClass<out TextConverter<*>> = StringTextConverter::class,
		vararg val polymorphisms:Polymorphism
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MixedToEitherBody(
		vararg val polymorphisms:Polymorphism
)

@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ParentElement

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RootElement(val name: String)

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BeforeSave

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class BeforeLoad

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AfterSave

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class AfterLoad

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
				type == String::class.starProjectedType -> StringTextConverter()
				type == Int::class.starProjectedType -> IntTextConverter()
				type == Boolean::class.starProjectedType ->
					if (nullable) TristateBoolTextConverter() else BoolTextConverter()
				type.isSubtypeOf(Enum::class.starProjectedType) -> {
					val to = HashMap<Enum<*>, String>()
					val from = HashMap<String, Enum<*>>()
					@Suppress("UNCHECKED_CAST")
					val klass = type.classifier as KClass<Enum<*>>
					for (enum in klass.java.enumConstants) {
						to[enum] = enum.name
						from[enum.name] = enum
					}
					MappedTextConverter(to, from)
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
	var parentElement: KMutableProperty1<T,in Any?>? = null
	for (property in clazz.declaredMemberProperties) {
		val pti by lazy { PropertyTypeinfo(property) }
		for (annotation in property.annotations) when (annotation) {
			is ParentElement -> {
				if (property !is KMutableProperty1<T,*>) error("@ParentElement $property must be mutable")
				if (parentElement != null) error("Duplicate @ParentElement $parentElement and $property")
				@Suppress("UNCHECKED_CAST")
				parentElement = property as KMutableProperty1<T, in Any?>
			}
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
			is PolymorphicElements -> {
				if (!pti.list) error("Cannot have @PolymorphicElements for non-list $property")
				val converter = PolymorphicElementConverter(TagPolymorphicPicker(
						annotation.polymorphisms.map { it.qualifier to it.klass }
				))
				if (annotation.wrapped) {
					val wrapperName = annotation.wrapperName ifEmpty property.name
					@Suppress("UNCHECKED_CAST")
					val eio = WrappedListPropertyEio(wrapperName,
					                                 converter,
					                                 property as KProperty1<T,MutableList<XmlSerializable>>)
					registerElementIO(eio,wrapperName)
				} else {
					@Suppress("UNCHECKED_CAST")
					val eio = ListPropertyEio(converter,
					                          property as KProperty1<T,MutableList<XmlSerializable>>)
					registerElementIO(eio, annotation.polymorphisms.map { it.qualifier })
				}
			}
			is MixedBody -> {
				if (!pti.list) error("Cannot have @PolymorphicElements for non-list $property")
				val converter = annotation.stringConverter.createInstance()
				@Suppress("UNCHECKED_CAST")
				mixedBody(property as KProperty1<T,MutableList<XmlSerializable>>,
				          converter as TextConverter<XmlSerializable>,
				          annotation.polymorphisms.map { it.qualifier to it.klass })
			}
			is MixedToEitherBody -> {
				if (!pti.list) error("Cannot have @PolymorphicElements for non-list $property")
				@Suppress("UNCHECKED_CAST")
				mixedBody(property as KProperty1<T,MutableList<Either<String, XmlSerializable>>>,
				          EitherLeftStringTextConverter(),
				          annotation.polymorphisms.map {it.qualifier to {EitherRightSzInfo(it.klass)} }
				          )
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
	for (function in clazz.declaredMemberFunctions) {
		for (annotation in function.annotations) when (annotation) {
			is BeforeSave -> {
				if (function.parameters.size != 1) error("@BeforeSave $function must be no-arg")
				val old = info.beforeSave
				function.isAccessible = true
				info.beforeSave = {
					old?.invoke(this)
					function.call(this)
				}
			}
			is AfterSave -> {
				if (function.parameters.size != 1) error("@AfterSave $function must be no-arg")
				val old = info.afterSave
				function.isAccessible = true
				info.afterSave = {
					old?.invoke(this)
					function.call(this)
				}
			}
			is BeforeLoad -> {
				if (function.parameters.size !in 1..2) error("@BeforeLoad $function must be no more than 1-arg")
				val old = info.beforeLoad
				function.isAccessible = true
				info.beforeLoad = {
					old?.invoke(this, it)
					when(function.parameters.size) {
						1 -> function.call(this)
						2 -> function.call(this, it)
					}
				}
			}
			is AfterLoad -> {
				if (function.parameters.size !in 1..2) error("@AfterLoad $function must be no more than 1-arg")
				val old = info.afterLoad
				function.isAccessible = true
				info.afterLoad = {
					old?.invoke(this, it)
					when(function.parameters.size) {
						1 -> function.call(this)
						2 -> function.call(this, it)
					}
				}
			}
		}
	}
	if (info.texti == null && info.elements.isEmpty()) emptyBody()
	parentElement?.let {
		beforeLoad { parent ->
			parentElement.set(this,parent)
		}
	}
}