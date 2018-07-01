package ej.mod

import ej.utils.affix
import ej.utils.affixNonEmpty
import ej.utils.crop
import javax.xml.bind.JAXBElement
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*
import javax.xml.namespace.QName


@XmlRegistry
class XContentRegistry {
	@XmlElementDecl(namespace = "", name = "set", scope = XContentContainer::class)
	fun createXContentContainerXsSet(value: XsSet): JAXBElement<XsSet> {
		return JAXBElement<XsSet>(QName("", "set"),
		                          XsSet::class.java,
		                          XContentContainer::class.java,
		                          value)
	}
}

interface XStatement {
	val tagName: String
	val emptyTag: Boolean get() = false
	fun innerXML(): String = ""
	fun attrsString(): String = ""
	fun toSourceString(): String =
			"<" + tagName + attrsString().affixNonEmpty(" ", "") + (
					if (emptyTag) "/>"
					else ">" + innerXML() + "</" + tagName + "/>"
					)
}

internal fun <T> List<T>.joinToSourceString() = joinToString("") {
	when (it) {
		is XStatement -> it.toSourceString()
		is String -> it
		else -> it.toString()
	}
}

interface StoryNode : XStatement {
	val name: String
}

enum class TrimMode {
	@XmlEnumValue("none") NONE,
	@XmlEnumValue("trim") TRIM,
	@XmlEnumValue("unindent") UNINDENT;
	
	fun applyTo(s:String):String {
		if (s.isEmpty()) return s
		val s2 = when(this) {
			TrimMode.NONE -> return s
			TrimMode.TRIM -> s.trim()
			TrimMode.UNINDENT -> s.trimIndent()
		}
		return s2
	}
}

// content =
//    text | b | i | font | xcc-statement | xxc-logic
// xcc-statement = display | set | output | lib # | xcc-named-content | Include | menu
// xxc-logic = if | switch
// scene-fin = menu | next | scenefin-if | battle
abstract class XContentContainer(override val tagName: String) : XStatement {
	override val emptyTag: Boolean = false
	
	@XmlElementRefs(
			XmlElementRef(name = "b", type = XcTextBold::class),
			XmlElementRef(name = "i", type = XcTextItalic::class),
			XmlElementRef(name = "font", type = XcTextStyled::class),
			XmlElementRef(name = "display", type = XsDisplay::class),
			XmlElementRef(name = "set", type = XsSet::class),
			XmlElementRef(name = "output", type = XsOutput::class),
			XmlElementRef(name = "lib", type = XcLib::class),
			XmlElementRef(name = "if", type = XlIf::class),
			XmlElementRef(name = "else", type = XlElse::class),
			XmlElementRef(name = "elseif", type = XlElseIf::class),
			XmlElementRef(name = "switch", type = XlSwitch::class),
			XmlElementRef(name = "menu", type = XsMenu::class),
			XmlElementRef(name = "next", type = XsNext::class),
			XmlElementRef(name = "battle", type = XsBattle::class)
	)
	@XmlMixed
	private val contentRaw:MutableList<Any> = ArrayList()
	
	@XmlAttribute(name="trim")
	internal var trimMode: TrimMode? = null // inherit
	
	val content:MutableList<XStatement> = ArrayList()
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		content.clear()
		content.addAll(contentRaw.map { it as? XStatement ?: XsTextNode(it.toString()) })
		contentRaw.clear()
		applyTrim(trimMode?:TrimMode.NONE)
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
		contentRaw.clear()
		contentRaw.addAll(content.map { (it as? XsTextNode)?.content ?: it })
	}
	
	override fun innerXML(): String = if (emptyTag) "" else contentRaw.joinToSourceString()
	override fun attrsString() = ""
	
	override fun toString() = toSourceString().crop(40)
}

private fun XStatement.applyTrim(trimMode: TrimMode) {
	if (trimMode == TrimMode.NONE) return
	val content = when (this) {
		is XcLib -> lib
		is XContentContainer -> content
		else -> return
	}
	for (stmt in content) {
		when (stmt) {
			is XsTextNode -> {
				val s = trimMode.applyTo(stmt.content)
				stmt.content = s
			}
			is XContentContainer -> {
				if (stmt.trimMode == null) stmt.applyTrim(trimMode)
			}
			is XcLib -> {
				if (stmt.trimMode == null) stmt.applyTrim(trimMode)
			}
		}
	}
	content.removeAll { (it is XsTextNode && it.content.isEmpty()) }
}

@XmlRootElement(name = "b")
class XcTextBold : XContentContainer("b")

@XmlRootElement(name = "i")
class XcTextItalic : XContentContainer("i")

@XmlRootElement(name = "font")
class XcTextStyled : XContentContainer("font") {
	@get:XmlAttribute
	var color: String? = null
	
	override fun attrsString() = color.affix("color='", "'")
}

@XmlRootElement(name = "lib")
class XcLib : StoryNode {
	@get:XmlAttribute
	override var name: String = ""
	@get:XmlElements(
			XmlElement(name = "lib", type = XcLib::class),
			XmlElement(name = "scene", type = XcScene::class),
			XmlElement(name = "text", type = XcNamedText::class)
	)
	val lib: ArrayList<StoryNode> = ArrayList()

	@XmlAttribute(name="trim")
	internal var trimMode: TrimMode? = null // inherit
	
	override val tagName: String get() = "lib"
	override val emptyTag: Boolean get() = false
	
	override fun innerXML(): String = lib.joinToSourceString()
	
	override fun attrsString(): String = "name='$name'"
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		applyTrim(trimMode?:TrimMode.NONE)
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
	}
}

@XmlRootElement(name = "scene")
class XcScene : XContentContainer("scene"), StoryNode {
	@get:XmlAttribute
	override var name: String = ""
	
	override fun attrsString() = "name='$name'"
}

class XcNamedText : XContentContainer("text"), StoryNode {
	@get:XmlAttribute
	override var name: String = ""
	
	override fun attrsString() = "name='$name'"
}