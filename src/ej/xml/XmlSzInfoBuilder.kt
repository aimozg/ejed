package ej.xml

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

class XmlSzInfoBuilder<T : Any>(name: String?,
                                internal val klass: KClass<T>) {
	internal val info = XmlSerializationInfo(klass)
	internal var nobody = false
	fun build(): XmlSerializationInfo<T> {
		if (!nobody && info.texti == null && info.elements.isEmpty()) {
			error("$klass: Must have emptyBody(), textBody(), mixedBody(), or element*()")
		}
		return info
	}
	
	init {
		if (name != null) info.name = name
	}
	
	var name: String?
		get() = info.name
		set(value) {
			info.name = value
		}
	val nameOrClass: String get() = info.nameOrClass
	
	fun handleAttribute(name: String,
	                    consumer: AttrConsumer<T>) {
		info.attri[name] = consumer
	}
	
	fun handleAttribute(name: String,
	                    handler: T.(String) -> Unit) {
		info.attri[name] = object : AttrConsumer<T> {
			override fun consumeAttr(obj: T, key: String, value: String) {
				obj.handler(value)
			}
		}
	}
	
	internal fun <R : Any> saveAttr(name: String,
	                       prop: KMutableProperty1<T, R>,
	                       converter: TextConverter<R>) {
		val aio = PropertyAio(name, converter, prop)
		info.attri[name] = aio
		info.attro += aio
	}
	
	internal fun <R : Any> saveAttrN(name: String,
	                        prop: KMutableProperty1<T, R?>,
	                        converter: TextConverter<R>) {
		val aio = NullablePropertyAio(name, converter, prop)
		info.attri[name] = aio
		info.attro += aio
	}
	
	@JvmName("attrString")
	fun attribute(prop: KMutableProperty1<T, String>, name: String = prop.name) {
		saveAttr(name, prop, StringConverter)
	}
	
	@JvmName("attrStringN")
	fun attribute(prop: KMutableProperty1<T, String?>, name: String = prop.name) {
		saveAttrN(name, prop, StringConverter)
	}
	
	@JvmName("attrInt")
	fun attribute(prop: KMutableProperty1<T, Int>, name: String = prop.name) {
		saveAttr(name, prop, IntConverter)
	}
	
	@JvmName("attrIntN")
	fun attribute(prop: KMutableProperty1<T, Int?>, name: String = prop.name) {
		saveAttrN(name, prop, IntConverter)
	}
	
	@JvmName("attrBool")
	fun attribute(prop: KMutableProperty1<T, Boolean>, name: String = prop.name) {
		saveAttr(name, prop, BoolConverter)
	}
	
	@JvmName("attrMapped")
	fun <E : Any> attribute(prop: KMutableProperty1<T, E>,
	                        to: Map<E, String>,
	                        from: Map<String, E>,
	                        name: String = prop.name) {
		saveAttr(name, prop, MappedConverter(to, from))
	}
	
	@JvmName("attrMapped")
	fun <E : Any> attribute(prop: KMutableProperty1<T, E>, mapping: Map<E, String>, name: String = prop.name) {
		attribute(prop, mapping, mapping.map { (k, v) -> v to k }.toMap(), name)
	}
	
	@JvmName("attrEnum")
	inline fun <reified E : Enum<E>> attribute(prop: KMutableProperty1<T, E>,
	                                           mapper: (E) -> String = { it.name },
	                                           name: String = prop.name) {
		attribute(prop, mutableMapOf<E, String>().apply {
			for (e in enumValues<E>()) {
				put(e, mapper(e))
			}
		}, name)
	}
	
	internal fun registerElementIO(eio: ElementIO<T>,
	                              names: Iterable<String>) {
		checkElement()
		names.forEach { info.elements[it] = eio }
		info.producers += eio
	}
	
	internal fun registerElementIO(eio: ElementIO<T>,
	                              name: String) {
		registerElementIO(eio, listOf(name))
	}
	
	fun handlerForElement(name: String): ElementConsumer<T>? {
		return info.elements[name]
	}
	
	fun handleElement(name: String, handler: ElementConsumer<T>) {
		info.elements[name] = handler
	}
	
	fun handleElement(name: String,
	                  handler: T.(tag: String, attrs: Map<String, String>, input: XmlExplorerController) -> Unit) {
		handleElement(name, object : ElementConsumer<T> {
			override fun consumeElement(obj: T, tag: String, attrs: Map<String, String>, input: XmlExplorerController) {
				obj.handler(tag, attrs, input)
			}
		})
	}
	
	@JvmName("elementString")
	fun element(prop: KMutableProperty1<T, String>, name: String = prop.name) {
		registerElementIO(PropertyEio(TextElementConverter(name, StringConverter), prop), name)
	}
	
	@JvmName("elementStringN")
	fun element(prop: KMutableProperty1<T, String?>, name: String = prop.name) {
		registerElementIO(NullablePropertyEio(TextElementConverter(name, StringConverter), prop), name)
	}
	
	@JvmName("elementBoolean")
	fun element(prop: KMutableProperty1<T, Boolean>, name: String = prop.name) {
		registerElementIO(PropertyEio(TextElementConverter(name, BoolConverter), prop), name)
	}
	
	@JvmName("elementSz")
	fun <R : Any> element(prop: KMutableProperty1<T, R>,
	                      szinfo: () -> XmlSerializationInfo<R>,
	                      name: String = prop.name) {
		registerElementIO(PropertyEio(XmlElementConverter(name, szinfo), prop), name)
	}
	
	@JvmName("elementSzKlass")
	fun <R : XmlSerializable> element(prop: KMutableProperty1<T, R>, klass: KClass<R>, name: String = prop.name) {
		element(prop, { getSerializationInfo(klass) }, name)
	}
	
	@JvmName("elementSzKlass")
	inline fun <reified R : XmlSerializable> element(prop: KMutableProperty1<T, R>, name: String = prop.name) {
		element(prop, R::class, name)
	}
	
	@JvmName("elementSzNullable")
	fun <R : Any> element(prop: KMutableProperty1<T, R?>,
	                      szinfo: () -> XmlSerializationInfo<R>,
	                      name: String = prop.name) {
		registerElementIO(NullablePropertyEio(XmlElementConverter(name, szinfo), prop), name)
	}
	
	@JvmName("elementSzNullableKlass")
	fun <R : XmlSerializable> element(prop: KMutableProperty1<T, R?>, klass: KClass<R>, name: String = prop.name) {
		element(prop, { getSerializationInfo(klass) }, name)
	}
	
	@JvmName("elementSzNullableKlass")
	inline fun <reified R : XmlSerializable> element(prop: KMutableProperty1<T, R?>, name: String = prop.name) {
		element(prop, R::class, name)
	}
	
	@JvmName("elementSzOverwrite")
	fun <R : XmlSerializable> elementOverwrite(prop: KProperty1<T, R>,
	                                           szinfo: () -> XmlSerializationInfo<R>,
	                                           name: String = prop.name) {
		registerElementIO(PropertyOverwritingEio(XmlOverwritingElementConverter(name, szinfo), prop), name)
	}
	
	@JvmName("elementSzKlass")
	fun <R : XmlSerializable> elementOverwrite(prop: KProperty1<T, R>, klass: KClass<R>, name: String = prop.name) {
		elementOverwrite(prop, { getSerializationInfo(klass) }, name)
	}
	
	@JvmName("elementSzKlass")
	inline fun <reified R : XmlSerializable> elementOverwrite(prop: KProperty1<T, R>, name: String = prop.name) {
		elementOverwrite(prop, R::class, name)
	}
	
	fun <R : XmlSerializable> elementByTag(prop: KMutableProperty1<T, R>,
	                                       vararg mappings: Pair<String, KClass<out R>>) {
		val pio = PropertyEio(PolymorphicElementConverter(TagPolymorphicPicker(mappings.asList())), prop)
		registerElementIO(pio, mappings.map { it.first })
	}
	
	@JvmName("elementByTagNullable")
	fun <R : XmlSerializable> elementByTag(prop: KMutableProperty1<T, R?>,
	                                       vararg mappings: Pair<String, KClass<out R>>) {
		val pio = NullablePropertyEio(PolymorphicElementConverter(TagPolymorphicPicker(mappings.asList())), prop)
		registerElementIO(pio, mappings.map { it.first })
	}
	
	fun <R : XmlSerializable> elementByAttr(prop: KMutableProperty1<T, R>,
	                                        tagname: String,
	                                        attrname: String,
	                                        vararg mappings: Pair<String, KClass<out R>>) {
		val pec = PolymorphicElementConverter(AttrPolymorphicPicker(tagname, attrname, mappings.asList()))
		val pio = PropertyEio(pec, prop)
		registerElementIO(pio, mappings.map { it.first })
	}
	
	@JvmName("elementByAttrNullable")
	fun <R : XmlSerializable> elementByAttr(prop: KMutableProperty1<T, R?>,
	                                        tagname:String,
	                                        attrname: String,
	                                        vararg mappings: Pair<String, KClass<out R>>) {
		val pec = PolymorphicElementConverter(AttrPolymorphicPicker(tagname, attrname, mappings.asList()))
		val pio = NullablePropertyEio(pec, prop)
		registerElementIO(pio, tagname)
	}
	
	
	fun <R : Any> elements(name: String,
	                       prop: KProperty1<T, MutableList<R>>,
	                       szinfo: () -> XmlSerializationInfo<R>) {
		registerElementIO(ListPropertyEio(XmlElementConverter(name, szinfo), prop), name)
	}
	
	fun <R : XmlSerializable> elements(name: String,
	                                   prop: KProperty1<T, MutableList<R>>,
	                                   klass: KClass<R>) {
		elements(name, prop) { getSerializationInfo(klass) }
	}
	
	inline fun <reified R : XmlSerializable> elements(name: String,
	                                                  prop: KProperty1<T, MutableList<R>>) {
		elements(name, prop, R::class)
	}
	
	fun <R : Any> wrappedElements(wrapper: String,
	                              name: String,
	                              prop: KProperty1<T, MutableList<R>>,
	                              szinfo: () -> XmlSerializationInfo<R>) {
		registerElementIO(WrappedListPropertyEio(wrapper, XmlElementConverter(name, szinfo), prop), wrapper)
	}
	
	fun <R : XmlSerializable> wrappedElements(wrapper: String,
	                                          name: String,
	                                          prop: KProperty1<T, MutableList<R>>,
	                                          klass: KClass<R>) {
		wrappedElements(wrapper, name, prop, { getSerializationInfo(klass) })
	}
	
	inline fun <reified R : XmlSerializable> wrappedElements(wrapper: String,
	                                                         name: String,
	                                                         prop: KProperty1<T, MutableList<R>>) {
		wrappedElements(wrapper, name, prop, R::class)
	}
	
	fun <R : Any> elementsByTag(prop: KProperty1<T, MutableList<R>>,
	                            mappings: List<Pair<String, () -> XmlSerializationInfo<out R>>>) {
		val eio = ListPropertyEio(PolymorphicElementConverter(TagPolymorphicPicker(mappings)), prop)
		registerElementIO(eio, mappings.map { it.first })
	}
	
	fun <R : XmlSerializable> elementsByTag(prop: KProperty1<T, MutableList<R>>,
	                                        mapping0: Pair<String, KClass<out R>>,
	                                        vararg mappings1: Pair<String, KClass<out R>>) {
		val mappings = listOf(mapping0) + mappings1
		val eio = ListPropertyEio(PolymorphicElementConverter(TagPolymorphicPicker(mappings)), prop)
		registerElementIO(eio, mappings.map { it.first })
	}
	
	fun emptyBody() {
		if (info.elements.isNotEmpty()) error("Cannot have both nobody() and element*()")
		if (info.texti != null) error("Cannot have both nobody() and textBody()")
		nobody = true
	}
	
	internal fun registerTextIO(tio:TextIO<T>) {
		info.texti = tio
		info.producers += tio
	}
	fun textBody(prop: KMutableProperty1<T, String>) {
		if (nobody) error("Cannot have both textBody() and emptyBody()")
		registerTextIO(PropertyTio(StringConverter, prop))
	}
	@JvmName("textBodyNullable")
	fun textBody(prop: KMutableProperty1<T, String?>) {
		if (nobody) error("Cannot have both textBody() and emptyBody()")
		registerTextIO(NullablePropertyTio(StringConverter, prop))
	}
	
	fun handleText(handler: TextConsumer<T>) {
		info.texti = handler
	}
	
	@JvmName("mixedBodyListMapped")
	fun <E : Any> mixedBody(prop: KProperty1<T, MutableList<E>>,
	                        toStr: (E?) -> String?,
	                        fromStr: (String) -> E,
	                        mappings: List<Pair<String, () -> XmlSerializationInfo<out E>>>) {
		if (info.texti != null) error("textBody() already set")
		if (nobody) error("Cannot have mixedBody() and emptyBody()")
		val mcc = MixedContentConverter(LambdaConverter(toStr, fromStr), TagPolymorphicPicker(mappings))
		val eio = ListPropertyEio(mcc, prop)
		info.texti = ListPropertyTio(mcc, prop)
		registerElementIO(eio, mappings.map { it.first })
	}
	
	@JvmName("mixedBodyListMappedKlass")
	fun <E : XmlSerializable> mixedBody(prop: KProperty1<T, MutableList<E>>,
	                                    toStr: (E?) -> String?,
	                                    fromStr: (String) -> E,
	                                    vararg mappings: Pair<String, KClass<out E>>) {
		mixedBody(prop, toStr, fromStr, mappings.map { (tag, klass) ->
			tag to { getSerializationInfo(klass) }
		})
	}
	
	fun afterLoad(fn: T.(Any?) -> Unit) {
		val old = info.afterLoad
		if (old == null) info.afterLoad = fn
		else info.afterLoad = { it ->
			old(it)
			fn(it)
		}
	}
	
	fun beforeSave(fn: T.() -> Unit) {
		val old = info.beforeSave
		if (old == null) info.beforeSave = fn
		else info.beforeSave = {
			old()
			fn()
		}
	}
	fun afterSave(fn: T.() -> Unit) {
		val old = info.afterSave
		if (old == null) info.afterSave = fn
		else info.afterSave = {
			old()
			fn()
		}
	}
	
	private fun checkElement() {
		if (nobody) error("Cannot have both element*() and emptyBody()")
	}
}

inline fun <reified T : Any> serializationInfo(
		name: String? = null,
		init: XmlSzInfoBuilder<T>.() -> Unit
): XmlSerializationInfo<T> =
		XmlSzInfoBuilder(name, T::class).apply(init).build()

fun <T : Any> serializationInfo(
		klass: KClass<T>,
		name: String? = null,
		init: XmlSzInfoBuilder<T>.() -> Unit
): XmlSerializationInfo<T> =
		XmlSzInfoBuilder(name, klass).apply(init).build()

inline fun <reified T : R, R : Any> serializationInfo(
		parent: XmlSerializationInfo<R>,
		name: String? = null,
		init: XmlSzInfoBuilder<T>.() -> Unit
): XmlSerializationInfo<T> =
		XmlSzInfoBuilder(name, T::class).apply {
			copySzInfo(parent, this)
			init()
		}.build()

fun <T : R, R : Any> copySzInfo(src: XmlSerializationInfo<R>, tgt: XmlSerializationInfo<T>) {
	tgt.attri.putAll(src.attri)
	tgt.texti = src.texti
	tgt.elements.putAll(src.elements)
	tgt.attro.addAll(src.attro)
	tgt.producers.addAll(src.producers)
	tgt.beforeSave = src.beforeSave
	tgt.afterLoad = src.afterLoad
}

fun <T : R, R : Any> copySzInfo(src: XmlSerializationInfo<R>, tgtb: XmlSzInfoBuilder<T>) {
	copySzInfo(src, tgtb.info)
}

fun <T : R, R : Any> XmlSzInfoBuilder<T>.inherit(from: XmlSerializationInfo<R>) {
	copySzInfo(from, info)
}

fun <T : R, R : Any> XmlSzInfoBuilder<T>.inherit(from: XmlSerializableCompanion<R>) {
	inherit(from.getSerializationInfo())
}

inline fun <T : R, reified R : XmlSerializable> XmlSzInfoBuilder<T>.inherit() {
	inherit(getSerializationInfo(R::class))
}

interface XmlSerializable
interface XmlSerializableCompanion<T : Any> {
	val szInfoClass: KClass<T>
	fun XmlSzInfoBuilder<T>.buildSzInfo()
}

internal fun <T : Any> XmlSerializableCompanion<T>.create() = XmlSzInfoBuilder(null, szInfoClass).apply {
	buildSzInfo()
}.build()

internal val KnownSzInfos = WeakHashMap<KClass<*>, XmlSerializationInfo<*>>()

fun <T : Any> getSerializationInfo(clazz: KClass<T>): XmlSerializationInfo<T> {
	return getSerializationInfoSafe(clazz) ?: error("No XmlSerializationInfo for $clazz")
}
@Suppress("UNCHECKED_CAST")
fun <T : Any> getSerializationInfoSafe(clazz: KClass<T>): XmlSerializationInfo<T>? {
	return KnownSzInfos.getOrPut(clazz) {
		val co = clazz.companionObjectInstance as? XmlSerializableCompanion<T>
		if (co?.szInfoClass == clazz) {
			co.create()
		} else if (clazz.isSubclassOf(XmlAutoSerializable::class)) {
			generateSerializationInfo(clazz as KClass<out XmlAutoSerializable>)
		} else {
			return null
		}
	} as XmlSerializationInfo<T>
}

fun <T : Any> XmlSerializableCompanion<T>.getSerializationInfo() = getSerializationInfo(szInfoClass)
