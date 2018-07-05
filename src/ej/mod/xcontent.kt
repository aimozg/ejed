package ej.mod

import ej.editor.utils.escapeXml
import ej.editor.utils.filteredIsInstanceMutable
import ej.utils.affixNonEmpty
import ej.utils.crop
import javafx.beans.property.ObjectProperty
import javafx.collections.ObservableList
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
		if (this is XcText) "" else
		"<$tagName" + attrsString().affixNonEmpty(" ") + (if (emptyTag) "/>" else ">")
internal fun XStatement.tagClose() =
		if (this is XcText || emptyTag) "" else "</$tagName>"

internal fun <T> List<T>.joinToSourceString() = joinToString("") {
	when (it) {
		is XStatement -> it.toSourceString()
		is XcText -> it.text
		is String -> it
		else -> it.toString()
	}
}

interface StoryContainer {
	val lib: ObservableList<StoryStmt>
}
interface StoryStmt : XStatement, StoryContainer {
	val name: String
	fun nameProperty(): ObjectProperty<String>
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

abstract class XContentContainer(override val tagName: String) : XStatement, StoryContainer {
	override val emptyTag: Boolean = false
	
	@XmlElementRefs(
			XmlElementRef(name = "t", type = XcText::class),
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
	
	override val lib = content.filteredIsInstanceMutable<StoryStmt>()
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		content.clear()
		content.addAll(contentRaw.map {
					it as? XStatement
					?: XcText(it.toString())
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
//		contentRaw.addAll(content.map { (it as? XcText)?.text ?: it })
		contentRaw.addAll(content)
	}
	
	override fun innerXML(): String = if (emptyTag) "" else contentRaw.joinToSourceString()
	override fun attrsString() = ""
	
	override fun toString() = toSourceString().crop(40)
}

class TrimmingVisitor(val trimMode: TrimMode) : ModVisitor() {
	override fun visitLib(x: XcLib) {
		if (x.trimMode == null) super.visitLib(x)
	}
	
	override fun visitAnyContentContainer(x: XContentContainer) {
		if (x.trimMode == null) super.visitAnyContentContainer(x)
	}
	
	override fun visitText(x: XcText) {
		x.text = (x.trimMode ?: trimMode).applyTo(x.text)
	}
}


@XmlRootElement(name = "t")
class XcText(text:String):XStatement {
	@get:XmlValue
	var text:String by property(text)
	fun textProperty() = getProperty(XcText::text)
	
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
	override var name by property("")
	override fun nameProperty() = getProperty(XcLib::name)
	
	@get:XmlElements(
			XmlElement(name = "lib", type = XcLib::class),
			XmlElement(name = "scene", type = XcScene::class),
			XmlElement(name = "text", type = XcNamedText::class)
	)
	override val lib = ArrayList<StoryStmt>().observable()

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
	override var name by property("")
	override fun nameProperty() = getProperty(XcScene::name)
	
	override fun attrsString() = "name='$name'"
}

@XmlRootElement(name="text")
class XcNamedText : XContentContainer("text"), StoryStmt {
	@get:XmlAttribute
	override var name by property("")
	override fun nameProperty() = getProperty(XcNamedText::name)
	
	override val lib = content.filteredIsInstanceMutable<StoryStmt>()
	
	override fun attrsString() = "name='$name'"
}

