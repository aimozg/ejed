package ej.mod

import ej.editor.utils.escapeXml
import ej.utils.affixNonEmpty
import ej.utils.crop
import tornadofx.*
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*


interface XStatement : ModDataNode {
	val tagName: String
	val emptyTag: Boolean get() = false
	fun innerXML(): String = ""
	fun attrsString(): String = ""
}

fun XStatement.toSourceString(): String =
		tagOpen() + innerXML() + tagClose()
internal fun XStatement.tagOpen() =
		if (this is XcUnstyledText) "" else
		"<$tagName" + attrsString().affixNonEmpty(" ") + (if (emptyTag) "/>" else ">")
internal fun XStatement.tagClose() =
		if (this is XcUnstyledText || emptyTag) "" else "</$tagName>"

internal fun <T> List<T>.joinToSourceString() = joinToString("") {
	when (it) {
		is XStatement -> it.toSourceString()
		is XcUnstyledText -> it.text
		is String -> it
		else -> it.toString()
	}
}

interface StoryStmt : XStatement {
	val name: String
	val lib: List<StoryStmt>
}

val REX_INDENT = Regex("""\n[ \t]++""")
val REX_CRONCE = Regex("""(?<!\n)\n(?!\n)""")
enum class TrimMode {
	@XmlEnumValue("none") NONE,
	@XmlEnumValue("trim") TRIM,
	@XmlEnumValue("unindent") UNINDENT;
	
	fun applyTo(s:String):String {
		if (s.isEmpty()) return s
		val s2 = when(this) {
			TrimMode.NONE -> return s
			TrimMode.TRIM -> s.trim()
			TrimMode.UNINDENT -> {
				// content = content;
				s.replace(REX_INDENT,"\n").replace(REX_CRONCE," ")
				// s.trimIndent()
			}
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
			XmlElementRef(name = "t", type = XcUnstyledText::class),
			XmlElementRef(name = "display", type = XsDisplay::class),
			XmlElementRef(name = "set", type = XsSet::class),
			XmlElementRef(name = "output", type = XsOutput::class),
			XmlElementRef(name = "lib", type = XcLib::class),
			XmlElementRef(name = "text", type = XcNamedText::class),
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
	
	@get:XmlAttribute(name="trim")
	internal var trimMode: TrimMode? = null // inherit
	
	val content = ArrayList<XStatement>().observable()
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		content.clear()
		content.addAll(contentRaw.map {
					it as? XStatement
					?: XcUnstyledText(it.toString())
		})
		contentRaw.clear()
		trimMode?.let { trimMode ->
			TrimmingVisitor(trimMode).visitAllStatements(content)
		}
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
		contentRaw.clear()
//		contentRaw.addAll(content.map { (it as? XcUnstyledText)?.text ?: it })
		contentRaw.addAll(content)
	}
	
	override fun innerXML(): String = if (emptyTag) "" else contentRaw.joinToSourceString()
	override fun attrsString() = ""
	
	override fun toString() = toSourceString().crop(40)
}

class TrimmingVisitor(val trimMode: TrimMode) : XModVisitor() {
	override fun visitLib(x: XcLib) {
		if (x.trimMode == null) super.visitLib(x)
	}
	
	override fun visitAnyContentContainer(x: XContentContainer) {
		if (x.trimMode == null) super.visitAnyContentContainer(x)
	}
	
	override fun visitText(x: XcUnstyledText) {
		x.text = (x.trimMode ?: trimMode).applyTo(x.text)
	}
}


@XmlRootElement(name = "t")
class XcUnstyledText(text:String):XStatement {
	@get:XmlValue
	var text:String by property(text)
	fun textProperty() = getProperty(XcUnstyledText::text)
	
	@get:XmlAttribute(name="trim")
	internal var trimMode: TrimMode? = null // inherit
	
	@Suppress("unused")
	constructor():this("")
	
	fun isEmpty():Boolean = text.isEmpty()
	
	override val tagName get() = "t"
	
	override fun innerXML(): String = text.escapeXml()
}

@XmlRootElement(name = "lib")
class XcLib : StoryStmt {
	@get:XmlAttribute
	override var name: String = ""
	@get:XmlElements(
			XmlElement(name = "lib", type = XcLib::class),
			XmlElement(name = "scene", type = XcScene::class),
			XmlElement(name = "text", type = XcNamedText::class)
	)
	override val lib: ArrayList<StoryStmt> = ArrayList()

	@XmlAttribute(name="trim")
	internal var trimMode: TrimMode? = null // inherit
	
	override val tagName: String get() = "lib"
	override val emptyTag: Boolean get() = false
	
	override fun innerXML(): String = lib.joinToSourceString()
	
	override fun attrsString(): String = "name='$name'"
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		trimMode?.let { trimMode ->
			TrimmingVisitor(trimMode).visitAllStatements(lib)
		}
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
	}
}

@XmlRootElement(name = "scene")
class XcScene : XContentContainer("scene"), StoryStmt {
	@get:XmlAttribute
	override var name: String = ""
	
	override val lib get() = content.filterIsInstance<StoryStmt>()
	
	override fun attrsString() = "name='$name'"
}

@XmlRootElement(name="text")
class XcNamedText : XContentContainer("text"), StoryStmt {
	@get:XmlAttribute
	override var name: String = ""
	
	override val lib get() = content.filterIsInstance<StoryStmt>()
	
	override fun attrsString() = "name='$name'"
}

