package ej.mod

import ej.xml.HasSzInfo
import ej.xml.XmlSerializable
import ej.xml.XmlSzInfoBuilder
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.io.Writer
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*
import kotlin.coroutines.experimental.buildSequence

/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

interface ModDataNode
interface XStatement : ModDataNode, XmlSerializable
interface XComplexStatement: XStatement {
	val content: ObservableList<XStatement>
}

@XmlRootElement(name="mod")
class ModData : ModDataNode, XmlSerializable {
	@get:XmlTransient
	var sourceFile: File? = null
	
	val nameProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var name:String  by nameProperty
	
	val versionProperty = SimpleIntegerProperty(0)
	@get:XmlAttribute
	var version: Int by versionProperty
	
	@get:XmlElementWrapper(name="state")
	@get:XmlElement(name="var")
	val stateVars: ObservableList<StateVar> = ArrayList<StateVar>().observable()
	
	@get:XmlElement(name="hook")
	val hooks: ObservableList<ModHookData> = ArrayList<ModHookData>().observable()
	
	@get:XmlElement(name="script")
	val scripts = ArrayList<ModScript>().observable()
	
	@get:XmlElement(name="monster")
	val monsters: ObservableList<MonsterData> = ArrayList<MonsterData>().observable()
	
	@get:XmlElements(
			XmlElement(name="lib",type=XcLib::class),
			XmlElement(name="scene",type=XcScene::class),
			XmlElement(name="text",type=XcNamedText::class)
	)
	val content: ObservableList<StoryStmt> = ArrayList<StoryStmt>().observable()
	
	fun allStories() = buildSequence {
		val run = ArrayList(content)
		while(run.isNotEmpty()) {
			val e = run.removeAt(0)
			yield(e)
			run.addAll(e.lib)
		}
	}
	
	@get:XmlElement(name="encounter")
	private val encounters = ArrayList<XmlEncounter>().observable()
	
	override fun toString(): String {
		return "<mod name='$name' version='$version'>" +
				" <state> ${stateVars.joinToString(" ")} </state>"+
				hooks.joinToString(" ")+
				monsters.joinToString(" ")+
				content.joinToString(" ")+
				"</mod>"
	}
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		content.addAll(encounters.map { e ->
			XcScene().also {s ->
				s.name = e.name
				s.owner = this@ModData
				s.content.addAll(e.scene.content)
				s.lib.addAll(e.scene.lib)
				s.trigger = EncounterTrigger().also { t ->
					t.chance = e.chance
					t.pool = e.pool
					t.condition = e.condition
				}
			}
		})
		visit(StylingVisitor())
	}
	
	companion object : HasSzInfo<ModData>{
		override val szInfoClass = ModData::class
		override fun XmlSzInfoBuilder<ModData>.buildSzInfo() {
			name = "mod"
			attr(ModData::name)
			attr(ModData::version)
			wrappedElements("state", "var", ModData::stateVars)
			elements("script", ModData::scripts)
			readElement("hook") { _, attrs, input ->
				val mod = this
				content.add(XcScene().also { s ->
					val trigger = TimedTrigger()
					s.trigger = trigger
					for ((k,v) in attrs) when(k) {
						"type" -> TODO("load hook type")
						"name" -> s.name = v
						else -> error("Unexpected hook@$k")
					}
					input.forEachElement { tag, _ ->
						when (tag) {
							"scene" -> TODO("load hook scene content")
							else -> error("Unexpected hook.$tag")
						}
					}
					s.owner = mod
				})
				TODO("convert hook to scene with trigger")
			}
			elements("script", ModData::monsters)
			readElement("encounter") { _, attrs, input ->
				val mod = this
				content.add(XcScene().also { s ->
					val trigger = EncounterTrigger()
					s.trigger = trigger
					for ((k,v) in attrs) when(k) {
						"pool" -> trigger.pool = v
						"name" -> s.name = v
						else -> error("Unexpected encounter@$k")
					}
					input.forEachElement { tag, _ ->
						when (tag) {
							"condition" -> trigger.condition = text()
							"chance" -> trigger.chance = text()
							"scene" -> TODO("load encounter scene content")
							else -> error("Unexpected encounter.$tag")
						}
					}
					s.owner = mod
				})
			}
			elementsByTag(ModData::content,
			              "scene" to XcScene::class,
			              "lib" to XcLib::class,
			              "text" to XcLib::class)
			afterLoad {
				visit(StylingVisitor())
			}
		}
		
		val jaxbContext: JAXBContext by lazy {
			JAXBContext.newInstance(ModData::class.java)
		}
		fun loadMod(src: InputStream):ModData {
//			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
			return unmarshaller().unmarshal(src) as ModData
		}
		fun loadMod(src: Reader):ModData {
//			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
			return unmarshaller().unmarshal(src) as ModData
		}
		fun saveMod(mod:ModData,dst: Writer) {
//			getSerializationInfo().serializeDocument(mod, XmlBuilder(dst))
			jaxbContext.createMarshaller().marshal(mod,dst)
		}
		
		fun unmarshaller() = jaxbContext.createUnmarshaller()
	}
	
}

class StylingVisitor : ReplacingVisitor() {
	override fun visitText(x: XcText) {
		if (x.isEmpty()) remove(x)
	}
	
	/*override fun visitAnyContentContainer(x: XContentContainer) {
		super.visitAnyContentContainer(x)
		var merged = false
		for ((i,stmt) in x.content.withIndex()) {
			val prev = if (i==0) null else x.content[i-1]
			if (prev is XcText && stmt is XcText) {
				stmt.text = prev.text + stmt.text
				prev.text = ""
				merged = true
			}
		}
		if (merged) x.content.removeAll { it is XcText && it.isEmpty()}
	}*/
}

val DefaultModData by lazy {
	ModData.loadMod(ModData::class.java.getResourceAsStream("default.xml"))
}
