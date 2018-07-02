package ej.mod

import ej.editor.utils.escapeXml
import ej.utils.affix
import ej.utils.affixNonEmpty
import ej.utils.crop
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*


interface XStatement {
	val tagName: String
	val emptyTag: Boolean get() = false
	fun innerXML(): String = ""
	fun attrsString(): String = ""
}

fun XStatement.toSourceString(): String =
		tagOpen() + innerXML() + tagClose()
internal fun XStatement.tagOpen() =
		if (this is XcTextNode) "" else
		"<$tagName" + attrsString().affixNonEmpty(" ") + (if (emptyTag) "/>" else ">")
internal fun XStatement.tagClose() =
		if (this is XcTextNode || emptyTag) "" else "</$tagName>"

internal fun <T> List<T>.joinToSourceString() = joinToString("") {
	when (it) {
		is XStatement -> it.toSourceString()
		is String -> it
		else -> it.toString()
	}
}

interface StoryStmt : XStatement {
	val name: String
	val lib: List<StoryStmt>
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
			XmlElementRef(name = "b", type = XmlElementB::class),
			XmlElementRef(name = "i", type = XmlElementI::class),
			XmlElementRef(name = "font", type = XmlElementFont::class),
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
	
	fun isTextOnly() = content.all { it is XcTextNode }
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		content.clear()
		content.addAll(contentRaw.map { it as? XStatement ?: XcTextNode(it.toString()) })
		contentRaw.clear()
		trimMode?.let { trimMode ->
			TrimmingVisitor(trimMode).visitAll(content)
			content.removeAll { (it is XcTextNode && it.content.isEmpty()) }
		}
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
		contentRaw.clear()
		contentRaw.addAll(content.map { (it as? XcTextNode)?.content ?: it })
	}
	
	override fun innerXML(): String = if (emptyTag) "" else contentRaw.joinToSourceString()
	override fun attrsString() = ""
	
	override fun toString() = toSourceString().crop(40)
}

class TrimmingVisitor(val trimMode: TrimMode) : XStatementVisitor() {
	override fun visitLib(stmt: XcLib) {
		if (stmt.trimMode == null) {
			visitAll(stmt.lib)
		}
	}
	
	override fun visitAnyContentContainer(stmt: XContentContainer) {
		if (stmt.trimMode == null) {
			visitAll(stmt.content)
			stmt.content.removeAll { (it is XcTextNode && it.content.isEmpty()) }
		}
	}
	
	override fun visitAnyStmt(stmt: XStatement) {
		if (stmt is XcTextNode) {
			stmt.content = trimMode.applyTo(stmt.content)
		}
	}
}

@XmlRootElement(name = "b")
class XmlElementB() : XContentContainer("b") {
	constructor(e:XStatement):this() {
		content += e
	}
}

@XmlRootElement(name = "i")
class XmlElementI() : XContentContainer("i") {
	constructor(e:XStatement):this() {
		content += e
	}
}

@XmlRootElement(name = "font")
class XmlElementFont() : XContentContainer("font") {
	constructor(e:XStatement,color:String?):this() {
		content += e
		this.color = color
	}

	@get:XmlAttribute
	var color: String? = null
	
	override fun attrsString() = color.affix("color='", "'")
}

class XcTextNode() : XStatement {
	constructor(content:String):this() {
		this.content = content
	}
	
	var content:String = ""
	
	override val tagName get() = ""
	override fun innerXML() = content.escapeXml()
	override fun toString() = toSourceString().crop(40)
	
}

class XcStyledText(): XStatement {
	constructor(run:Run):this() {
		addRun(run)
	}
	
	fun addRun(content: String, bold: Boolean=false, italic: Boolean=false, color: String?=null) {
		runs.add(Run(content, bold, italic, color))
	}
	fun addRun(run:Run) {
		runs.add(run)
	}
	
	var htmlContent:String
		get() = runs.fold(ArrayList<XStatement>()) { rslt, run ->
			//val prev = rslt.lastOrNull()
			var e:XStatement = XcTextNode(run.content)
			if (run.bold) e = XmlElementB(e)
			if (run.italic) e = XmlElementI(e)
			if (run.color != null) e = XmlElementFont(e,run.color)
			rslt.add(e)
			rslt
		}.joinToSourceString()
		set(value) {
			runs.clear()
			addRun(value)
		}
	
	class Run(var content:String,var bold:Boolean=false,var italic:Boolean=false,var color:String?=null) {
		fun sameStyleAs(other:Run) =
				this.bold == other.bold
						&& this.italic == other.italic
						&& this.color == other.color
	}
	val runs = ArrayList<Run>()
	override val tagName: String get() = ""
	override fun toString() = toSourceString().crop(40)
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
			TrimmingVisitor(trimMode).visitAll(lib)
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

class XcNamedText : XContentContainer("text"), StoryStmt {
	@get:XmlAttribute
	override var name: String = ""
	
	override val lib get() = content.filterIsInstance<StoryStmt>()
	
	override fun attrsString() = "name='$name'"
}

