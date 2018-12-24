package ej.mod

import ej.editor.utils.escapeXmlAttr
import ej.utils.affixNonEmpty
import ej.utils.crop
import ej.xml.*
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.StringWriter


@Suppress("unused")
internal fun ModDataNode.defaultToString(tagname:String, attrs:String, content:String) =
		"[" + tagname + attrs.affixNonEmpty("(",")") + content.affixNonEmpty(": ") + "]"

fun XStatement.defaultNamedSzInfo(): Pair<String, AXmlSerializationInfo<XStatement>>? {
	val myInfo = getSerializationInfo(javaClass.kotlin)
	val name = myInfo.name
	if (name != null) return name to myInfo
	return XContentContainer.statementMappings.entries.find {
		it.value() == myInfo
	}?.let {
		it.key to myInfo
	}
}

fun defaultNamedSzInfo(name: String): AXmlSerializationInfo<out XStatement>? {
	return XContentContainer.statementMappings[name]?.invoke()
}

fun XStatement.toXml(builder: XmlBuilder) {
	val myInfo = defaultNamedSzInfo()
			?: kotlin.error("Unsaveable statement $this")
	myInfo.second.serializeDocument(this, myInfo.first, builder)
}
fun XStatement.toXmlObject(): XmllikeObject =
		XmlObjectBuilder().also { it -> toXml(it) }.build()

fun XStatement.toPrettyPrintedXml(withDocHeader: Boolean = true): String {
	val writer = StringWriter()
	val builder = IndentingXmlBuilder(XmlStreamBuilder(writer), ModData.MOD_INDENT_STYLE)
	toXml(builder)
	var s: CharSequence = writer.buffer
	if (!withDocHeader) s = s.replace(Regex("""^<\?xml[^>]+\?>\s*+"""), "")
	return s.toString()
}


fun XStatementFromXmlObject(src: XmllikeObject): XStatement =
		XmlObjectExplorer(src).exploreDocument { rootTag, rootAttrs ->
			val szinfo = defaultNamedSzInfo(rootTag)
					?: error("Unknown statement tag $rootTag")
			szinfo.deserialize(this, rootAttrs, null)
		}
		
open class XContentContainer : XComplexStatement {
	
	final override val content: ObservableList<XStatement> = ArrayList<XStatement>().observable()
	private val contentRaw = ArrayList<XStatement>()
	
	override fun toString() = defaultToString("content")
	
	companion object : XmlSerializableCompanion<XContentContainer> {
		override val szInfoClass= XContentContainer::class
		
		val statementMappings: Map<String, SzInfoMaker<out XStatement>> = mapOf(
				"t" to XcText::class,
				"display" to XsDisplay::class,
				"forward" to XsForward::class,
				"set" to XsSet::class,
				"command" to XsCommand::class,
				"output" to XsOutput::class,
				"if" to XmlFlatIf::class,
				"else" to XmlFlatElse::class,
				"elseif" to XmlFlatElseif::class,
				"if0" to XlIf::class,
				"switch" to XlSwitch::class,
				"comment" to XlComment::class,
				"menu" to XsMenu::class,
				"next" to XsNext::class,
				"button" to XsButton::class,
				"battle" to XsBattle::class
		).mapValues { { getSerializationInfo(it.value) } }
		
		override fun XmlSzInfoBuilder<XContentContainer>.buildSzInfo() {
			beforeSave {
				contentRaw.addAll(content.map { e -> (e as? XlIf)?.ungrouped() ?: e})
			}
			afterSave {
				contentRaw.clear()
			}
			mixedBody(XContentContainer::contentRaw,
			          { (it as? XcText)?.text },
			          { if (it.isBlank()) XcText("") else XcText(it) },
			          statementMappings
			)
			afterLoad {
				content.addAll(contentRaw.mapNotNull { e ->
					when (e) {
						is XmlFlatIf -> e.grouped()
						is XcText -> if (e.isEmpty()) null else e
						else -> e
					}
				})
				contentRaw.clear()
			}
		}
		
	}
}
internal fun XContentContainer.defaultToString(tagname: String, attrs: String="") =
		defaultToString(tagname,attrs,content.joinToString(" ",limit=5))


class XcText(text: String) : XStatement, XmlAutoSerializable {
	@TextBody
	var text:String by property(text)
	fun textProperty() = getProperty(XcText::text)
	
	@Suppress("unused")
	constructor():this("")
	
	fun isEmpty():Boolean = text.isEmpty()
	
	override fun toString() = "\""+text.crop(40).escapeXmlAttr()+"\""
	
	@DefaultElementConsumer
	@Suppress("unused")
	private fun unknownElement(tag: String, attrs: Map<String, String>, input: XmlExplorerController) {
		when (tag.toLowerCase()) {
			"u", "i", "b", "font" -> {
				text += "<$tag"
				if (attrs.isNotEmpty()) text += attrs.entries.joinToString { (k, v) -> " $k=\"${v.escapeXmlAttr()}\"" }
				text += ">"
				input.forEachNode { (textNode, elementNode) ->
					if (textNode != null) text += textNode
					if (elementNode != null) unknownElement(elementNode.first, elementNode.second, input)
				}
				text += "</tag>"
			}
			else -> input.error("unexpected element t.$tag")
		}
	}
}

class XcLib : StoryStmt {
	override val nameProperty = SimpleStringProperty("")
	override var name:String by nameProperty
	
	override val isValidProperty = SimpleObjectProperty(ValidationStatus.UNKNOWN)
	override var isValid: ValidationStatus by isValidProperty

	override var owner:ModDataNode? = null
	
	override val lib = ArrayList<StoryStmt>().observable()
	
	override fun toString() = defaultToString("lib","name='$name'",lib.joinToString(" ",limit=5))
	
	companion object : XmlSerializableCompanion<XcLib> {
		override val szInfoClass = XcLib::class
		
		override fun XmlSzInfoBuilder<XcLib>.buildSzInfo() {
			attribute(XcLib::name)
			elementsByTag(XcLib::lib,
			              "lib" to XcLib::class,
			              "scene" to XcScene::class,
			              "text" to XcNamedText::class)
			afterLoad { parent ->
				owner = parent as ModDataNode?
			}
		}
		
	}
}

class XcScene : XContentContainer(), StoryStmt {
	override val nameProperty = SimpleStringProperty("")
	override var name:String by nameProperty
	override val lib = ArrayList<StoryStmt>().observable()
	
	override val isValidProperty = SimpleObjectProperty(ValidationStatus.UNKNOWN)
	override var isValid: ValidationStatus by isValidProperty
	
	override var owner:ModDataNode? = null
	
	val triggerProperty = SimpleObjectProperty<SceneTrigger?>()
	var trigger:SceneTrigger? by triggerProperty

	override fun toString() = defaultToString("scene","name='$name'",lib.joinToString(" "))
	
	companion object : XmlSerializableCompanion<XcScene> {
		override val szInfoClass = XcScene::class
		
		override fun XmlSzInfoBuilder<XcScene>.buildSzInfo() {
			inherit(XContentContainer)
			name = "scene"
			elementsByTag(XcScene::lib,
			              "lib" to XcLib::class,
			              "text" to XcNamedText::class,
			              "scene" to XcScene::class
			)
			attribute(XcScene::name)
			elementByAttr(XcScene::trigger, "trigger", "triggerType",
			              "encounter" to EncounterTrigger::class,
			              "timed" to TimedTrigger::class,
			              "place" to PlaceTrigger::class)
			afterLoad { parent ->
				owner = parent as ModDataNode?
			}
		}
		
	}
}

class XcNamedText : XContentContainer(), StoryStmt {
	override val nameProperty = SimpleStringProperty("")
	override var name:String by nameProperty
	override val lib = ArrayList<StoryStmt>().observable()
	
	override val isValidProperty = SimpleObjectProperty(ValidationStatus.UNKNOWN)
	override var isValid: ValidationStatus by isValidProperty
	
	override var owner:ModDataNode? = null
	
	override fun toString() = defaultToString("text","name='$name'",lib.joinToString(" "))
	
	companion object : XmlSerializableCompanion<XcNamedText> {
		override val szInfoClass = XcNamedText::class
		
		override fun XmlSzInfoBuilder<XcNamedText>.buildSzInfo() {
			inherit(XContentContainer)
			name = "text"
			elementsByTag(XcNamedText::lib,
			              "lib" to XcLib::class,
			              "text" to XcNamedText::class,
			              "scene" to XcScene::class
			)
			attribute(XcNamedText::name)
			afterLoad { parent ->
				owner = parent as ModDataNode?
			}
		}
	}
}

