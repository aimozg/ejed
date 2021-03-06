package ej.xml

import ej.utils.iAmEitherLeft
import org.funktionale.either.Either

interface TextConverter<A : Any> {
	fun convert(s: String): A?
	fun toString(a: A?): String?
}
interface ElementConverter<A:Any> {
	fun convert(tag: String,
	            attrs: Map<String, String>,
	            input: XmlExplorerController,
	            parent: Any?): A?
	fun write(builder:XmlBuilder, value:A)
	fun producesText(): Boolean = false
	fun producesElements(): Boolean = true
}
interface OverwritingElementConverter<A:XmlSerializable>: ElementConverter<A> {
	fun convertInto(obj:A,
	                tag: String,
	                attrs: Map<String, String>,
	                input: XmlExplorerController,
	                parent: Any?)
	
}
class TextElementConverter<A:Any>(val tag:String,val t:TextConverter<A>) : ElementConverter<A> {
	override fun convert(tag: String,
	                     attrs: Map<String, String>,
	                     input: XmlExplorerController,
	                     parent: Any?): A? {
		return t.convert(input.text())
	}
	
	override fun write(builder: XmlBuilder, value: A) {
		val body = t.toString(value)
		if (body != null) builder.element(tag, body)
	}
	
}

class LambdaTextConverter<A : Any>(
		val serializeFn: (A?) -> String?,
		val deserializeFn: (String) -> A
) : TextConverter<A> {
	override fun toString(a: A?): String? = serializeFn(a)
	override fun convert(s: String): A = deserializeFn(s)
}

class StringTextConverter(
		val whitespacePolicy: WhitespacePolicy = WhitespacePolicy.KEEP
) : TextConverter<String> {
	override fun toString(a: String?) = a
	override fun convert(s: String) = whitespacePolicy.applyTo(s)
}

class EitherLeftStringTextConverter<R>
@JvmOverloads constructor(
		val whitespacePolicy: WhitespacePolicy = WhitespacePolicy.KEEP
) : TextConverter<Either<String, R>> {
	override fun convert(s: String) =
			whitespacePolicy.applyTo(s).takeIf { it.isNotEmpty() }?.iAmEitherLeft()
	
	override fun toString(a: Either<String, R>?): String? = a?.component1()
}

class IntTextConverter : TextConverter<Int> {
	override fun toString(a: Int?) = a?.toString()
	override fun convert(s: String) = s.trim().toInt()
}

class DoubleTextConverter : TextConverter<Double> {
	override fun toString(a: Double?) = a?.toString()
	override fun convert(s: String) = s.trim().toDouble()
}

class BoolTextConverter : TextConverter<Boolean> {
	override fun toString(a: Boolean?) = if (a == true) "true" else null
	override fun convert(s: String) = s.trim() == "true"
}

class TristateBoolTextConverter : TextConverter<Boolean> {
	override fun toString(a: Boolean?) = when (a) {
		true -> "true"
		false -> "false"
		else -> null
	}
	override fun convert(s: String) = s == "true"
}

class MappedTextConverter<E : Any>(val to: Map<E, String>, val from:Map<String,E>) : TextConverter<E> {
	override fun toString(a: E?): String? = if (a == null) null else to[a]
	override fun convert(s: String): E = from[s] ?: error("Enum value $s not found")
}

typealias SzInfoMaker<E> = ()->AXmlSerializationInfo<E>

open class XmlElementConverter<E: Any>(val tag:String, szinfoFactory: SzInfoMaker<E>) : ElementConverter<E> {
	
	val szinfo by lazy(szinfoFactory)
	override fun convert(tag: String,
	                     attrs: Map<String, String>,
	                     input: XmlExplorerController,
	                     parent: Any?): E =
			szinfo.deserialize(input, attrs, parent)
	
	override fun write(builder: XmlBuilder, value: E) {
		szinfo.serialize(value, tag, builder)
	}
}
class XmlOverwritingElementConverter<E: XmlSerializable>(val tag:String, szinfoFactory: SzInfoMaker<E>) : OverwritingElementConverter<E> {
	override fun convertInto(obj: E,
	                         tag: String,
	                         attrs: Map<String, String>,
	                         input: XmlExplorerController,
	                         parent: Any?) {
		szinfo.deserializeInto(obj, input,attrs,parent)
	}
	
	private val aszinfo by lazy(szinfoFactory)
	val szinfo get() = aszinfo as XmlSerializationInfo<E>
	override fun convert(tag: String,
	                     attrs: Map<String, String>,
	                     input: XmlExplorerController,
	                     parent: Any?): E =
			szinfo.deserialize(input, attrs, parent)
	
	override fun write(builder: XmlBuilder, value: E) {
		szinfo.serialize(value, tag, builder)
	}
}

