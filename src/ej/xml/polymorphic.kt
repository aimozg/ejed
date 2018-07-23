package ej.xml

import kotlin.reflect.KClass

abstract class PolymorphicPicker<E:Any>(
		val mappings: List<Pair<String, SzInfoMaker<out E>>>
) {
	abstract fun pick(tag: String, attrs: Map<String,String>): XmlSerializationInfo<out E>?
	open fun pick(e:E) = mappings.find {
		it.second().accepts(e)
	}?.let {
		it.first to it.second()
	}
	open fun pickAndDeserialize(input: XmlExplorerController, tag:String, attrs:Map<String,String>, parent:Any?): E? {
		return pick(tag,attrs)?.deserialize(input,attrs,parent)
	}
	open fun serializeIfAccepts(value: E, builder: XmlBuilder):Boolean {
		val pick = pick(value) ?: return false
		write(value, pick, builder)
		return true
	}
	abstract fun write(value:E, pick:Pair<String, XmlSerializationInfo<out E>>, builder: XmlBuilder)
}

class TagPolymorphicPicker<E:Any>(
		mappings: List<Pair<String, SzInfoMaker<out E>>>
): PolymorphicPicker<E>(mappings) {
	override fun pick(tag: String, attrs: Map<String, String>) = mappings.find {
		it.first == tag
	}?.second?.invoke()
	
	override fun write(value: E, pick: Pair<String, XmlSerializationInfo<out E>>, builder: XmlBuilder) {
		pick.second.serializeIfAccepts(value, pick.first, builder)
	}
}

fun <E : XmlSerializable> TagPolymorphicPicker(mappings: List<Pair<String, KClass<out E>>>) = TagPolymorphicPicker(
		mappings.map {
			it.first to {
				getSerializationInfo(it.second)
			}
		})

class AttrPolymorphicPicker<E:Any>(
		val tagname:String,
		val attrname:String,
		mappings: List<Pair<String, SzInfoMaker<out E>>>
): PolymorphicPicker<E>(mappings) {
	override fun pick(tag: String, attrs: Map<String, String>) = mappings.find {
		attrs[attrname] == it.first
	}?.second?.invoke()
	
	override fun pickAndDeserialize(input: XmlExplorerController,
	                                tag: String,
	                                attrs: Map<String, String>,
	                                parent: Any?): E? {
		val pick = pick(tag,attrs) ?: return null
		return pick.deserialize(input,attrs - attrname,parent)
	}
	
	override fun write(value: E, pick: Pair<String, XmlSerializationInfo<out E>>, builder: XmlBuilder) {
		pick.second.serializeIfAccepts(value, tagname, builder) { it + mapOf(attrname to pick.first) }
	}
}

fun <E : XmlSerializable> AttrPolymorphicPicker(tagname:String, attrname:String, mappings: List<Pair<String, KClass<out E>>>) = AttrPolymorphicPicker(
		tagname,
		attrname,
		mappings.map {
			it.first to {
				getSerializationInfo(it.second)
			}
		})

open class PolymorphicElementConverter<E: Any>(
		val picker: PolymorphicPicker<E>
): ElementConverter<E> {
	protected fun tryConvert(tag: String, attrs: Map<String, String>, input: XmlExplorerController, parent: Any?): E? {
		return picker.pickAndDeserialize(input,tag,attrs,parent)
	}
	override fun convert(tag: String, attrs: Map<String, String>, input: XmlExplorerController, parent: Any?): E {
		return tryConvert(tag,attrs,input,parent) ?: error("Unexpected tag $tag")
	}
	
	override fun write(builder: XmlBuilder, value: E) {
		if (tryWrite(builder, value)) return
		error("No mapping for $value")
	}
	
	protected fun tryWrite(builder: XmlBuilder, value: E): Boolean {
		return picker.serializeIfAccepts(value,builder)
	}
	
}

class MixedContentConverter<E: Any>(
		val textConverter: TextConverter<E>,
		picker: PolymorphicPicker<E>
): PolymorphicElementConverter<E>(picker) {
	
	override fun write(builder: XmlBuilder, value: E) {
		if (!tryWrite(builder, value)) {
			val s = textConverter.toString(value)
			if (s != null) builder.text(s)
			else error("No mapping for $value")
		}
	}
}