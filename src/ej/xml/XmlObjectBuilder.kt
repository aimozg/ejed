package ej.xml

import ej.utils.peek
import ej.utils.pop
import ej.utils.push
import org.funktionale.either.Either
import java.io.*

class XmllikeObject(
		val name: String,
		attrs0: Map<String, String> = emptyMap(),
		body0: List<XmllikeNode> = emptyList()
) {
	constructor(name: String, attrs0: Map<String, String>, vararg body0: XmllikeNode) :
			this(name, attrs0, body0.asList())
	
	val attributes = LinkedHashMap<String, String>(attrs0)
	val body = ArrayList<XmllikeNode>(body0)
	
	fun toBytes(): ByteArray = ByteArrayOutputStream().also { baos ->
		serialize(this, DataOutputStream(baos))
	}.toByteArray()
	
	override fun equals(other: Any?): Boolean {
		if (this === other) return true
		if (javaClass != other?.javaClass) return false
		
		other as XmllikeObject
		
		if (name != other.name) return false
		if (attributes != other.attributes) return false
		if (body != other.body) return false
		
		return true
	}
	
	override fun hashCode(): Int {
		var result = name.hashCode()
		result = 31 * result + attributes.hashCode()
		result = 31 * result + body.hashCode()
		return result
	}
	
	override fun toString(): String {
		return "<$name" +
				attributes.entries.joinToString(separator = "") { (k, v) -> " $k=\"$v\"" } +
				(if (body.isEmpty()) "/>"
				else body.joinToString(prefix = ">", separator = "", postfix = "</$name>"))
	}
	
	
	companion object {
		const val MAGIC_TEXT = 1
		const val MAGIC_ELEMENT = 2
		const val MAGIC_END = 3
		private fun writeHeader(obj: XmllikeObject, dst: DataOutput) {
			dst.writeUTF(obj.name)
			dst.writeInt(obj.attributes.size)
			for ((k, v) in obj.attributes) {
				dst.writeUTF(k)
				dst.writeUTF(v)
			}
		}
		
		fun serialize(obj: XmllikeObject, dst: DataOutput) {
			val stack = ArrayList<Iterator<XmllikeNode>>()
			
			writeHeader(obj, dst)
			stack.push(obj.body.iterator())
			while (stack.isNotEmpty()) {
				val peek = stack.peek()
				if (peek.hasNext()) {
					val (l, r) = peek.next()
					if (l != null) {
						dst.writeByte(MAGIC_TEXT)
						dst.writeUTF(l)
					} else if (r != null) {
						dst.writeByte(MAGIC_ELEMENT)
						writeHeader(r, dst)
						stack.push(r.body.iterator())
					}
				} else {
					dst.writeByte(MAGIC_END)
					stack.pop()
				}
			}
		}
		
		private fun readHeader(src: DataInput): XmllikeObject {
			val name = src.readUTF()
			val attrcount = src.readInt()
			val obj = XmllikeObject(name)
			for (i in 1..attrcount) {
				val key = src.readUTF()
				val value = src.readUTF()
				obj.attributes[key] = value
			}
			return obj
		}
		
		fun deserialize(src: DataInput): XmllikeObject {
			val stack = ArrayList<MutableListIterator<XmllikeNode>>()
			
			val root = readHeader(src)
			stack.push(root.body.listIterator())
			while (stack.isNotEmpty()) {
				val body = stack.peek()
				val magic = src.readByte().toInt()
				when (magic) {
					MAGIC_TEXT -> {
						val data = src.readUTF()
						body.add(Either.left(data))
					}
					MAGIC_ELEMENT -> {
						val elem = readHeader(src)
						stack.push(elem.body.listIterator())
						body.add(Either.right(elem))
					}
					MAGIC_END -> {
						stack.pop()
					}
					else -> throw IOException("Bad magic $magic")
				}
			}
			return root
		}
		
		fun fromBytes(src: ByteArray): XmllikeObject =
				deserialize(DataInputStream(ByteArrayInputStream(src)))
	}
}
typealias XmllikeNode = Either<String, XmllikeObject>

class XmlObjectBuilder : XmlBuilder {
	private var state = 0
	private val stack = ArrayList<ArrayList<XmllikeNode>>()
	private var root: XmllikeObject? = null
	private fun check(method: String) {
		if (state == 0) error("$method before startDocument()")
		if (state == 2) error("$method after endDocument()")
	}
	
	override fun startDocument() {
		if (state != 0) error("Invalid call to startDocument()")
		state = 1
	}
	
	override fun endDocument() {
		if (state != 1) error("Invalid call to endDocument()")
		if (root == null) error("No root element")
		if (stack.isNotEmpty()) error("Element not closed")
		state = 2
	}
	
	override fun startElement(tag: String, attrs: Map<String, String>) {
		check("startElement()")
		val element = XmllikeObject(tag, attrs)
		if (root == null) {
			root = element
		} else {
			stack.peek() += Either.right(element)
		}
		stack.push(element.body)
	}
	
	override fun endElement() {
		check("endElement()")
		if (stack.isEmpty()) error("Unexpected endElement()")
		stack.pop()
	}
	
	override fun text(data: String) {
		check("text()")
		if (root == null) error("No root element")
		stack.peek() += Either.left(data)
	}
	
	fun build(): XmllikeObject {
		if (state != 2) error("build() before endDocument()")
		return root ?: error("build() before endDocument()")
	}
}