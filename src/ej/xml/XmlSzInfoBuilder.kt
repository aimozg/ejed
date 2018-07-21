package ej.xml

import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.KProperty1
import kotlin.reflect.full.companionObjectInstance

class XmlSzInfoBuilder<T : Any>(name:String?,
                                internal val klass: KClass<T>) {
	internal val info = XmlSerializationInfo(klass)
	internal var nobody = false
	fun build(): XmlSerializationInfo<T> {
		if (!nobody && info.texti == null && info.elements.isEmpty()) {
			error("$klass: Must have noBody(), text(), texts(), or element*()")
		}
		return info
	}
	init {
		if (name != null) info.name = name
	}
	
	var name:String?
		get() = info.name
		set(value) { info.name = value }
	fun readAttr(name:String,
	             consumer:AttrConsumer<T>) {
		info.attri[name] = consumer
	}
	fun readAttr(name:String,
	             handler:T.(String)->Unit) {
		info.attri[name] = object:AttrConsumer<T> {
			override fun consumeAttr(obj: T, key: String, value: String) {
				obj.handler(value)
			}
		}
	}
	fun<R:Any> saveAttr(name: String,
	                    prop: KMutableProperty1<T, R>,
	                    converter: TextConverter<R>) {
		val aio = PropertyAio(name, converter, prop)
		info.attri[name] = aio
		info.attro += aio
	}
	fun<R:Any> saveAttrN(name: String,
	                    prop: KMutableProperty1<T, R?>,
	                    converter: TextConverter<R>) {
		val aio = NullablePropertyAio(name, converter, prop)
		info.attri[name] = aio
		info.attro += aio
	}
	
	@JvmName("attrString")
	fun attr(prop: KMutableProperty1<T, String>, name: String = prop.name) {
		saveAttr(name, prop, StringConverter)
	}
	
	@JvmName("attrStringN")
	fun attr(prop: KMutableProperty1<T, String?>, name: String = prop.name) {
		saveAttrN(name,prop,StringConverter)
	}
	
	@JvmName("attrInt")
	fun attr(prop: KMutableProperty1<T, Int>, name: String = prop.name) {
		saveAttr(name, prop, IntConverter)
	}
	
	@JvmName("attrIntN")
	fun attr(prop: KMutableProperty1<T, Int?>, name: String = prop.name) {
		saveAttrN(name,prop,IntConverter)
	}
	
	@JvmName("attrBool")
	fun attr(prop: KMutableProperty1<T, Boolean>, name: String = prop.name) {
		saveAttr(name, prop, BoolConverter)
	}
	
	@JvmName("attrMapped")
	fun <E : Any> attr(prop: KMutableProperty1<T, E>,
	                   to: Map<E, String>,
	                   from: Map<String, E>,
	                   name: String = prop.name) {
		saveAttr(name, prop, MappedConverter(to, from))
	}
	
	@JvmName("attrMapped")
	fun <E : Any> attr(prop: KMutableProperty1<T, E>, mapping: Map<E, String>, name: String = prop.name) {
		attr(prop, mapping, mapping.map { (k,v)->v to k }.toMap(), name)
	}
	
	@JvmName("attrEnum")
	inline fun <reified E : Enum<E>> attr(prop: KMutableProperty1<T, E>, mapper:(E)->String = {it.name}, name:String = prop.name) {
		attr(prop, mutableMapOf<E, String>().apply {
			for (e in enumValues<E>()) {
				put(e, mapper(e))
			}
		}, name)
	}
	
	private fun<R:Any> saveElement(name: String,
	                    prop: KMutableProperty1<T, R>,
	                    converter: ElementConverter<R>) {
		checkElement()
		val eio = PropertyEio(converter, prop)
		info.elements[name] = eio
		info.producers += eio
	}
	private fun<R:Any> saveElementN(name: String,
	                    prop: KMutableProperty1<T, R?>,
	                    converter: ElementConverter<R>) {
		checkElement()
		val eio = NullablePropertyEio(converter, prop)
		info.elements[name] = eio
		info.producers += eio
	}
	fun readElement(name:String,handler:ElementConsumer<T>) {
		info.elements[name] = handler
	}
	fun readElement(name:String,handler:T.(tag: String, attrs: Map<String, String>, input: XmlExplorerController)->Unit) {
		info.elements[name] = object:ElementConsumer<T> {
			override fun consumeElement(obj: T, tag: String, attrs: Map<String, String>, input: XmlExplorerController) {
				obj.handler(tag,attrs,input)
			}
		}
	}
	
	@JvmName("elementString")
	fun element(prop: KMutableProperty1<T, String>, name: String = prop.name) {
		saveElement(name, prop, TextElementConverter(name,StringConverter))
	}
	
	@JvmName("elementStringN")
	fun element(prop: KMutableProperty1<T, String?>, name: String = prop.name) {
		saveElementN(name, prop, TextElementConverter(name,StringConverter))
	}
	
	@JvmName("elementBoolean")
	fun element(prop: KMutableProperty1<T, Boolean>, name: String = prop.name) {
		saveElement(name, prop, TextElementConverter(name,BoolConverter))
	}
	
	@JvmName("elementSz")
	fun<R:Any> element(prop: KMutableProperty1<T, R>, szinfo:()->XmlSerializationInfo<R>, name: String = prop.name) {
		saveElement(name, prop, XmlElementConverter(name, szinfo))
	}
	@JvmName("elementSzKlass")
	fun<R:XmlSerializable> element(prop: KMutableProperty1<T, R>, klass:KClass<R>, name: String = prop.name) {
		element(prop, { getSerializationInfo(klass)}, name)
	}
	@JvmName("elementSzNullable")
	fun<R:Any> element(prop: KMutableProperty1<T, R?>, szinfo:()->XmlSerializationInfo<R>, name: String = prop.name) {
		saveElementN(name, prop, XmlElementConverter(name, szinfo))
	}
	@JvmName("elementSzNullableKlass")
	fun<R:XmlSerializable> element(prop: KMutableProperty1<T, R?>, klass:KClass<R>, name: String = prop.name) {
		element(prop, { getSerializationInfo(klass)}, name)
	}
	
	private fun<R:Any> saveElements(prop: KProperty1<T, MutableList<R>>, converter:ElementConverter<R>, name:String) {
		checkElement()
		val eio = ListPropertyEio(converter, prop)
		info.elements[name] = eio
		info.producers += eio
	}
	
	fun<R:Any> elements(name: String,
	                    prop: KProperty1<T, MutableList<R>>,
	                    szinfo: () -> XmlSerializationInfo<R>) {
		saveElements(prop,XmlElementConverter(name, szinfo),name)
	}
	fun<R:XmlSerializable> elements(name: String,
	                                prop: KProperty1<T, MutableList<R>>,
	                                klass: KClass<R>) {
		elements(name, prop, { getSerializationInfo(klass)})
	}
	inline fun<reified R:XmlSerializable> elements(name: String,
	                                               prop: KProperty1<T, MutableList<R>>) {
		elements(name, prop, R::class)
	}
	fun<R:Any> wrappedElements(wrapper: String,
	                           name: String,
	                           prop: KProperty1<T, MutableList<R>>,
	                           szinfo: () -> XmlSerializationInfo<R>) {
		checkElement()
		val eio = WrappedListPropertyEio(wrapper, XmlElementConverter(name, szinfo), prop)
		info.elements[wrapper] = eio
		info.producers += eio
	}
	fun<R:XmlSerializable> wrappedElements(wrapper: String,
	                                       name: String,
	                                       prop: KProperty1<T, MutableList<R>>,
	                                       klass: KClass<R>) {
		checkElement()
		wrappedElements(wrapper, name, prop, { getSerializationInfo(klass)})
	}
	inline fun<reified R:XmlSerializable> wrappedElements(wrapper: String,
	                                                      name: String,
	                                                      prop: KProperty1<T, MutableList<R>>) {
		wrappedElements(wrapper, name, prop, R::class)
	}
	
	fun<R:Any> elementsByTag(prop: KProperty1<T, MutableList<R>>,
	                         mappings:List<Pair<String,()->XmlSerializationInfo<out R>>>) {
		checkElement()
		val pio = PolymorphicListIO(prop,mappings)
		for ((tag, _) in mappings) {
			info.elements[tag] = pio
		}
		info.producers += pio
	}
	
	fun <R : XmlSerializable> elementsByTag(prop: KProperty1<T, MutableList<R>>,
	                                        mapping0: Pair<String, KClass<out R>>,
	                                        vararg mappings: Pair<String, KClass<out R>>) {
		elementsByTag(prop,
		              (listOf(mapping0) + mappings)
				              .map { (tag, klass) ->
					              tag to { getSerializationInfo(klass)}
				              }
		)
	}

	fun noBody() {
		if (info.elements.isNotEmpty()) error("Cannot have both nobody() and element*()")
		if (info.texti != null) error("Cannot have both nobody() and text()")
		nobody = true
	}
	
	fun text(prop: KMutableProperty1<T, String>) {
		if (nobody) error("Cannot have both text() and noBody()")
		val tio = PropertyTio(StringConverter, prop)
		info.texti = tio
		info.producers += tio
	}
	
	@JvmName("mixedBodyListMapped")
	fun<E:Any> mixed(prop: KProperty1<T, MutableList<E>>,
	                 toStr:(E?)->String?,
	                 fromStr:(String)->E,
	                 mappings:List<Pair<String,()->XmlSerializationInfo<out E>>>) {
		if (info.texti != null) error("textBody() already set")
		if (nobody) error("Cannot have mixed() and nobody()")
		val mio = MixedListIO(prop, LambdaConverter(toStr, fromStr), mappings)
		info.texti = mio
		for ((tag, _) in mappings) {
			info.elements[tag] = mio
		}
		info.producers += mio
	}
	@JvmName("mixedBodyListMappedKlass")
	fun<E:XmlSerializable> mixed(prop: KProperty1<T, MutableList<E>>,
	                             toStr:(E?)->String?,
	                             fromStr:(String)->E,
	                             vararg mappings:Pair<String,KClass<out E>>) {
		mixed(prop, toStr, fromStr, mappings.map { (tag,klass) ->
			tag to { getSerializationInfo(klass) }
		})
	}
	
	fun afterLoad(fn:T.(Any?)->Unit) {
		val old = info.afterLoad
		if (old == null) info.afterLoad = fn
		else info.afterLoad = {it ->
			old(it)
			fn(it)
		}
	}
	fun beforeSave(fn:T.()->Unit) {
		val old = info.beforeSave
		if (old == null) info.beforeSave = fn
		else info.beforeSave = {
			old()
			fn()
		}
	}
	
	private fun checkElement() {
		if (nobody) error("Cannot have both element*() and noBody()")
	}
}

inline fun <reified T : Any> serializationInfo(
		name:String? = null,
		init: XmlSzInfoBuilder<T>.() -> Unit
): XmlSerializationInfo<T> =
		XmlSzInfoBuilder(name,T::class).apply(init).build()

inline fun <reified T : R, R:Any> serializationInfo(
		parent: XmlSerializationInfo<R>,
		name:String? = null,
		init: XmlSzInfoBuilder<T>.() -> Unit
): XmlSerializationInfo<T> =
		XmlSzInfoBuilder(name,T::class).apply{
			copySzInfo(parent,this)
			init()
		}.build()

fun<T:R,R:Any> copySzInfo(src:XmlSerializationInfo<R>, tgt:XmlSerializationInfo<T>) {
	tgt.attri.putAll(src.attri)
	tgt.texti = src.texti
	tgt.elements.putAll(src.elements)
	tgt.attro.addAll(src.attro)
	tgt.producers.addAll(src.producers)
	tgt.beforeSave = src.beforeSave
	tgt.afterLoad = src.afterLoad
}
fun<T:R,R:Any> copySzInfo(src:XmlSerializationInfo<R>, tgtb:XmlSzInfoBuilder<T>) {
	copySzInfo(src, tgtb.info)
}
fun<T:R,R:Any> XmlSzInfoBuilder<T>.inherit(from:XmlSerializationInfo<R>) {
	copySzInfo(from, info)
}
fun<T:R,R:Any> XmlSzInfoBuilder<T>.inherit(from:HasSzInfo<R>) {
	inherit(from.getSerializationInfo())
}
inline fun<T:R,reified R:XmlSerializable> XmlSzInfoBuilder<T>.inherit() {
	inherit(getSerializationInfo(R::class))
}

interface XmlSerializable
interface HasSzInfo<T:Any> {
	val szInfoClass: KClass<T>
	fun XmlSzInfoBuilder<T>.buildSzInfo()
}
internal fun<T:Any> HasSzInfo<T>.create() = XmlSzInfoBuilder(null,szInfoClass).apply {
	buildSzInfo()
}.build()
internal val KnownSzInfos = WeakHashMap<KClass<*>,XmlSerializationInfo<*>>()

@Suppress("UNCHECKED_CAST")
fun<T:Any> getSerializationInfo(clazz: KClass<T>): XmlSerializationInfo<T> {
	return KnownSzInfos.getOrPut(clazz) {
		val co = clazz.companionObjectInstance as? HasSzInfo<T>
		if (co?.szInfoClass == clazz) {
			co.create()
		} else {
			error("No XmlSerializationInfo for $clazz")
		}
	} as XmlSerializationInfo<T>
}
fun<T:Any> HasSzInfo<T>.getSerializationInfo() = getSerializationInfo(szInfoClass)
