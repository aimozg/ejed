package ej.mod

import ej.editor.utils.escapeXml
import ej.utils.affix
import ej.utils.affixNonEmpty
import ej.utils.crop
import javafx.scene.paint.Color
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import tornadofx.*
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*
import javax.xml.bind.annotation.adapters.XmlAdapter
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter


interface XStatement : ModDataNode {
	val tagName: String
	val emptyTag: Boolean get() = false
	fun innerXML(): String = ""
	fun attrsString(): String = ""
}

fun XStatement.toSourceString(): String =
		tagOpen() + innerXML() + tagClose()
internal fun XStatement.tagOpen() =
		if (this is XcStyledText) "" else
		"<$tagName" + attrsString().affixNonEmpty(" ") + (if (emptyTag) "/>" else ">")
internal fun XStatement.tagClose() =
		if (this is XcStyledText || emptyTag) "" else "</$tagName>"

internal fun <T> List<T>.joinToSourceString() = joinToString("") {
	when (it) {
		is XStatement -> it.toSourceString()
		is XcStyledText.Run -> it.toSourceString()
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
			XmlElementRef(name = "b", type = XmlElementB::class),
			XmlElementRef(name = "i", type = XmlElementI::class),
			XmlElementRef(name = "font", type = XmlElementFont::class),
			XmlElementRef(name = "t", type = XcStyledText::class),
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
		content.addAll(contentRaw.map { it as? XStatement ?: XcStyledText(it.toString()) })
		contentRaw.clear()
		trimMode?.let { trimMode ->
			TrimmingVisitor(trimMode).visitAllStatements(content)
		}
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		trimMode = null
		contentRaw.clear()
//		contentRaw.addAll(content.map { (it as? XcTextNode)?.content ?: it })
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
	
	override fun visitText(x: XcStyledText) {
		for (run in x.runs) {
			run.content = trimMode.applyTo(run.content)
		}
	}
}

class StylingVisitor : ReplacingVisitor() {
	var style = XcStyledText.Style()
	override fun visitText(x: XcStyledText) {
		if (x.isEmpty()) remove(x)
		else x.applyStyle(style)
	}
	
	override fun visitXmlB(x: XmlElementB) {
		restyle(x, style.copy(bold=true))
	}
	override fun visitXmlI(x: XmlElementI) {
		restyle(x, style.copy(italic=true))
	}
	override fun visitXmlFont(x: XmlElementFont) {
		restyle(x, style.copy(color=x.color))
	}
	fun restyle(x: XContentContainer, newStyle:XcStyledText.Style) {
		val oldStyle = style
		style = newStyle
		replace(x, x.content)
		visitAnyContentContainer(x)
		style = oldStyle
	}
	
	override fun visitAnyContentContainer(x: XContentContainer) {
		super.visitAnyContentContainer(x)
		var merged = false
		for ((i,stmt) in x.content.withIndex()) {
			val prev = if (i==0) null else x.content[i-1]
			if (prev is XcStyledText && stmt is XcStyledText) {
				stmt.runs.addAll(0,prev.runs)
				stmt.compact()
				prev.runs.clear()
				merged = true
			}
		}
		if (merged) x.content.removeAll { it is XcStyledText && it.isEmpty() }
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

@XmlRootElement(name = "t")
class XcStyledText(text:String,style:Style=Style()): XStatement {
	
	class Run(
			@get:XmlValue
			var content:String,
			@get:XmlAttribute
			var style:Style) {
		@Suppress("unused")
		constructor():this("", Style())
		fun sameStyleAs(other:Run) = this.style == other.style
		fun isEmpty() = content.isEmpty()
		fun isBlank() = content.isBlank()
		fun toSourceString(): String {
			var s = content.escapeXml().replace("\n","<br>")
			style.color?.let { color -> s = "<font color='$color'>$s</font>" }
			if (style.bold) s = "<b>$s</b>"
			if (style.italic) s = "<i>$s</i>"
			return s
		}
	}
	
	@get:XmlElement(name="r")
	val runs = ArrayList<Run>()
	
	@Suppress("unused")
	constructor():this("", Style())

	@XmlJavaTypeAdapter(value=StyleAdapter::class)
	data class Style(val bold:Boolean=false,val italic:Boolean=false, val color:String?=null) {
		fun isEmpty():Boolean {
			return !bold && !italic && color == null
		}
		
		fun combine(other: Style) =
				if (this == other) other else
					Style(bold = other.bold || bold,
					      italic = other.italic || italic,
					      color = other.color ?: color)
		
		fun toCss() : String = InlineCss().also { css ->
			if (bold) css.fontWeight = FontWeight.BOLD
			if (italic) css.fontStyle = FontPosture.ITALIC
			if (color != null) css.fill = Color.web(color)
		}.render()
	}
	class StyleAdapter:XmlAdapter<String?,Style>() {
		override fun marshal(v: Style): String? {
			if (v.isEmpty()) return null
			return  (if (v.bold)"b" else "") +
					(if (v.italic)"i" else "") +
					v.color.affix("c", ";")
			
		}
		
		override fun unmarshal(v: String?): Style {
			if (v == null) return Style()
			val m = STYLE_REGEX.matchEntire(v) ?: kotlin.error("Invalid style format $v")
			return Style(
					bold = m.groupValues[1].isNotEmpty(),
					italic = m.groupValues[2].isNotEmpty(),
					color = m.groupValues[3].takeIf { it.isNotEmpty() }
			)
		}
		companion object {
			val STYLE_REGEX = Regex("(b?)(i?)(?:c([^;]++);)?")
		}
	}
	
	fun isEmpty():Boolean {
		return runs.none { it.content.isNotEmpty() }
	}
	fun applyStyle(style: Style) {
		if (style.isEmpty()) return
		for (run in runs) {
			run.style = run.style.combine(style)
		}
	}
	
	fun addRun(content: String, style:Style? = null) {
		runs.add(Run(content, style ?: Style()))
	}
	fun addRun(run:Run) {
		runs.add(run)
	}
	
	@get:XmlTransient
	val htmlContent:String
		get() =  runs.joinToSourceString()
	/*
	@get:XmlTransient
	var htmlContent:String
		get() = runs.fold(ArrayList<XStatement>()) { rslt, run ->
			//val prev = rslt.lastOrNull()
			var e:XStatement = XcTextNode(run.content)
			if (run.style.bold) e = XmlElementB(e)
			if (run.style.italic) e = XmlElementI(e)
			if (run.style.color != null) e = XmlElementFont(e,run.style.color)
			rslt.add(e)
			rslt
		}.joinToSourceString()
		set(value) {
			runs.clear()
			addRun(value)
		}
	*/
	@get:XmlTransient
	var textContent:String
		get() = runs.joinToString(""){it.content}
		set(value) {
			runs.clear()
			addRun(value)
		}
	
	override val tagName: String get() = "t"
	override fun innerXML() = runs.joinToString { it.content.crop(10).escapeXml().affix("[","]") }
	override fun toString() = toSourceString()
	fun compact() {
		var del = false
		for ((prev,next) in runs.zipWithNext()) {
			if (prev.isBlank() || next.isBlank() || next.sameStyleAs(prev)) {
				if (next.isBlank()) next.style = prev.style
				next.content = prev.content + next.content
				prev.content = ""
				del = true
			}
		}
		if (del) runs.removeAll { it.isEmpty() }
		if (runs.isEmpty()) addRun("")
	}
	init {
		addRun(Run(text,style))
	}
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

class XcNamedText : XContentContainer("text"), StoryStmt {
	@get:XmlAttribute
	override var name: String = ""
	
	override val lib get() = content.filterIsInstance<StoryStmt>()
	
	override fun attrsString() = "name='$name'"
}

