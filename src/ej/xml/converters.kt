package ej.xml

interface TextConverter<A : Any> {
	fun convert(s: String): A
	fun toString(a: A?): String?
}
interface ElementConverter<A:Any> {
	fun convert(tag: String,
	            attrs: Map<String, String>,
	            input: XmlExplorerController,
	            parent: Any?): A
	fun write(builder:XmlBuilder, value:A)
}
class TextElementConverter<A:Any>(val tag:String,val t:TextConverter<A>) : ElementConverter<A> {
	override fun convert(tag: String,
	                     attrs: Map<String, String>,
	                     input: XmlExplorerController,
	                     parent: Any?): A {
		return t.convert(input.text())
	}
	
	override fun write(builder: XmlBuilder, value: A) {
		val body = t.toString(value)
		if (body != null) builder.element(tag, body)
	}
	
}

class LambdaConverter<A : Any>(
		val serializeFn: (A?) -> String?,
		val deserializeFn: (String) -> A
) : TextConverter<A> {
	override fun toString(a: A?): String? = serializeFn(a)
	override fun convert(s: String): A = deserializeFn(s)
}

object StringConverter : TextConverter<String> {
	override fun toString(a: String?) = a
	override fun convert(s: String) = s
}

object IntConverter : TextConverter<Int> {
	override fun toString(a: Int?) = a?.toString()
	override fun convert(s: String) = s.toInt()
}

object BoolConverter : TextConverter<Boolean> {
	override fun toString(a: Boolean?) = if (a == true) "true" else null
	override fun convert(s: String) = s == "true"
}

class MappedConverter<E : Any>(val to: Map<E, String>, val from:Map<String,E>) : TextConverter<E> {
	override fun toString(a: E?): String? = if (a == null) null else to[a]
	override fun convert(s: String): E = from[s] ?: error("Enum value $s not found")
}

class XmlElementConverter<E: Any>(val tag:String, szinfoFactory: ()->XmlSerializationInfo<E>) : ElementConverter<E> {
	
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

