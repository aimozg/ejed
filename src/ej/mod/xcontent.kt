package ej.mod

import ej.utils.affix
import ej.utils.affixNonEmpty
import javafx.collections.ObservableList
import tornadofx.*
import javax.xml.bind.JAXBElement
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

// content =
//    text | b | i | font | xcc-statement | xxc-logic
// xcc-statement = display | set | output | lib # | xcc-named-content | Include | menu
// xxc-logic = if | switch
// scene-fin = menu | next | scenefin-if | battle
open class XContentContainer(override val tagName: String) : XStatement {
	override val emptyTag: Boolean = false
	
	@get:XmlElementRefs(
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
	@get:XmlMixed
	val contentRaw:ObservableList<Any> = observableList()
	
	val content:List<XStatement> = ArrayList<XStatement>().apply {
		bind(contentRaw) { it: Any ->
			it as? XStatement ?: XsTextNode(it.toString())
		}
	}
	
	override fun innerXML(): String = if (emptyTag) "" else contentRaw.joinToSourceString()
	override fun attrsString() = ""
	
	override fun toString() = toSourceString()
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
	
//	override fun toString() = toSourceString()
	
	override val tagName: String get() = "lib"
	override val emptyTag: Boolean get() = false
	
	override fun innerXML(): String = lib.joinToSourceString()
	
	override fun attrsString(): String = "name='$name'"
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