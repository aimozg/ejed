package ej.mod

import ej.editor.utils.escapeXmlAttr
import ej.utils.affixNonEmpty
import ej.utils.crop
import ej.utils.removeLast
import ej.xml.HasSzInfo
import ej.xml.XmlSerializable
import ej.xml.XmlSzInfoBuilder
import ej.xml.inherit
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ObservableList
import tornadofx.*
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*


val XStatement.acceptsMenu: Boolean
	get() = this is XcScene
val XStatement.acceptsActions: Boolean
	get() = acceptsMenu || this is XcNamedText

@Suppress("unused")
internal fun XStatement.defaultToString(tagname:String, attrs:String, content:String) =
		"[" + tagname + attrs.affixNonEmpty("(",")") + content.affixNonEmpty(": ") + "]"

interface StoryContainer {
	val lib: ObservableList<StoryStmt>
}
interface StoryStmt : XStatement, StoryContainer, XmlSerializable {
	@get:XmlTransient
	val owner: ModDataNode?
	@get:XmlTransient
	val name: String
	fun nameProperty(): Property<String>
	@get:XmlTransient
	val path:String get() = ownersToRoot().fold(name) { s, story ->
			"${story.name}/$s"
		}
}
fun StoryStmt.ownersToRoot() = generateSequence(owner as? StoryStmt) {
	it.owner as? StoryStmt
}
fun StoryStmt.pathRelativeTo(other:StoryStmt):String {
	val myOwners = ownersToRoot().toMutableList()
	val otherOwners = other.ownersToRoot().toMutableList()
	while (myOwners.isNotEmpty() && otherOwners.isNotEmpty()) {
		if (myOwners.last() == otherOwners.last()) {
			myOwners.removeLast()
			otherOwners.removeLast()
		} else {
			break
		}
	}
	if (myOwners.lastOrNull() == other) myOwners.removeLast()
	return ("../".repeat(otherOwners.size)) + myOwners.fold(name) { s, story ->
		"${story.name}/$s"
	}
}

abstract class XContentContainer : XComplexStatement, StoryContainer {
	
	@XmlElementRefs(
			XmlElementRef(name = "t", type = XcText::class),
			XmlElementRef(name = "display", type = XsDisplay::class),
			XmlElementRef(name = "forward", type = XsForward::class),
			XmlElementRef(name = "set", type = XsSet::class),
			XmlElementRef(name = "output", type = XsOutput::class),
			XmlElementRef(name = "lib", type = XcLib::class),
			XmlElementRef(name = "text", type = XcNamedText::class),
			XmlElementRef(name = "scene", type = XcScene::class),
			XmlElementRef(name = "if", type = XmlFlatIf::class),
			XmlElementRef(name = "else", type = XmlFlatElse::class),
			XmlElementRef(name = "elseif", type = XmlFlatElseif::class),
			XmlElementRef(name = "switch", type = XlSwitch::class),
			XmlElementRef(name = "comment", type = XlComment::class),
			XmlElementRef(name = "menu", type = XsMenu::class),
			XmlElementRef(name = "next", type = XsNext::class),
			XmlElementRef(name = "button", type = XsButton::class),
			XmlElementRef(name = "battle", type = XsBattle::class)
	)
	@XmlMixed
	private val contentRaw:MutableList<Any> = ArrayList()
	
	final override val content: ObservableList<XStatement> = ArrayList<XStatement>().observable()
	
	override val lib = ArrayList<StoryStmt>().observable()
	
	@Suppress("unused", "UNUSED_PARAMETER")
	protected open fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any?){
		val stmts = contentRaw.map {
			(it as? XmlFlatIf)?.grouped()
					?: it as? XStatement
					?: XcText(it.toString())
		}
		content.clear()
		content.addAll(stmts.filter { it !is StoryStmt })
		lib.clear()
		lib.addAll(stmts.filterIsInstance<StoryStmt>())
		contentRaw.clear()
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	protected open fun beforeMarshal(marshaller: Marshaller) {
		contentRaw.clear()
		contentRaw.addAll(lib)
		contentRaw.addAll(content.map {
			(it as? XlIf)?.ungrouped() ?: it
		})
	}
	override fun toString() = defaultToString("content")
	
	companion object : HasSzInfo<XContentContainer> {
		override val szInfoClass= XContentContainer::class
		
		override fun XmlSzInfoBuilder<XContentContainer>.buildSzInfo() {
			elementsByTag(XContentContainer::lib,
			              "lib" to XcLib::class,
			              "text" to XcNamedText::class,
			              "scene" to XcScene::class
			         )
			mixed(XContentContainer::content,
			      {(it as? XcText)?.text},
			      {XcText(it)},
			      "t" to XcText::class,
			      "display" to XsDisplay::class,
			      "forward" to XsForward::class,
			      "set" to XsSet::class,
			      "output" to XsOutput::class,
			      "if" to XmlFlatIf::class,
			      "else" to XmlFlatElse::class,
			      "elseif" to XmlFlatElseif::class,
			      "switch" to XlSwitch::class,
			      "comment" to XlComment::class,
			      "menu" to XsMenu::class,
			      "next" to XsNext::class,
			      "button" to XsButton::class,
			      "battle" to XsBattle::class
			      )
		}
		
	}
}
internal fun XContentContainer.defaultToString(tagname: String, attrs: String="") =
		defaultToString(tagname,attrs,content.joinToString(" ",limit=5))


@XmlRootElement(name = "t")
class XcText(text:String):XStatement {
	@get:XmlValue
	var text:String by property(text)
	fun textProperty() = getProperty(XcText::text)
	
	@Suppress("unused")
	constructor():this("")
	
	fun isEmpty():Boolean = text.isEmpty()
	
	override fun toString() = "\""+text.crop(40).escapeXmlAttr()+"\""
}

@XmlRootElement(name = "lib")
class XcLib : StoryStmt {
	@get:XmlAttribute
	override var name by property("")
	override fun nameProperty() = getProperty(XcLib::name)
	
	@get:XmlTransient
	override var owner:ModDataNode? = null
	
	@get:XmlElements(
			XmlElement(name = "lib", type = XcLib::class),
			XmlElement(name = "scene", type = XcScene::class),
			XmlElement(name = "text", type = XcNamedText::class)
	)
	override val lib = ArrayList<StoryStmt>().observable()
	
	override fun toString() = defaultToString("lib","name='$name'",lib.joinToString(" ",limit=5))
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		owner = parent as ModDataNode?
	}
}

@XmlRootElement(name = "scene")
class XcScene : XContentContainer(), StoryStmt {
	@get:XmlAttribute
	override var name:String by property("")
	override fun nameProperty() = getProperty(XcScene::name)
	
	@get:XmlTransient
	override var owner:ModDataNode? = null
	
	val triggerProperty = SimpleObjectProperty<SceneTrigger?>()
	var trigger:SceneTrigger? by triggerProperty

	@Suppress("unused", "UNUSED_PARAMETER")
	override fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any?){
		super.afterUnmarshal(unmarshaller,parent)
		owner = parent as ModDataNode?
	}
	
	override fun toString() = defaultToString("scene","name='$name'",lib.joinToString(" "))
	
	companion object : HasSzInfo<XcScene> {
		override val szInfoClass = XcScene::class
		
		override fun XmlSzInfoBuilder<XcScene>.buildSzInfo() {
			inherit(XContentContainer)
			attr(XcScene::name)
			
		}
		
	}
}

@XmlRootElement(name="text")
class XcNamedText : XContentContainer(), StoryStmt {
	@get:XmlAttribute
	override var name by property("")
	override fun nameProperty() = getProperty(XcNamedText::name)
	
	@get:XmlTransient
	override var owner:ModDataNode? = null
	
	@Suppress("unused", "UNUSED_PARAMETER")
	override fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any?){
		super.afterUnmarshal(unmarshaller,parent)
		owner = parent as ModDataNode?
	}
	
	override fun toString() = defaultToString("text","name='$name'",lib.joinToString(" "))
}

