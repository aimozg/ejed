package ej.mod

import ej.xml.*
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.io.Writer
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

class ModData : ModDataNode, XmlSerializable {
	var sourceFile: File? = null
	
	val nameProperty = SimpleStringProperty("")
	var name:String  by nameProperty
	
	val versionProperty = SimpleIntegerProperty(0)
	var version: Int by versionProperty
	
	val stateVars: ObservableList<StateVar> = ArrayList<StateVar>().observable()
	
	val scripts = ArrayList<ModScript>().observable()
	
	val monsters: ObservableList<MonsterData> = ArrayList<MonsterData>().observable()
	
	val content: ObservableList<StoryStmt> = ArrayList<StoryStmt>().observable()
	
	fun allStories() = buildSequence {
		val run = ArrayList(content)
		while(run.isNotEmpty()) {
			val e = run.removeAt(0)
			yield(e)
			run.addAll(e.lib)
		}
	}
	
	override fun toString(): String {
		return "<mod name='$name' version='$version'>" +
				" <state> ${stateVars.joinToString(" ")} </state>"+
				monsters.joinToString(" ")+
				content.joinToString(" ")+
				"</mod>"
	}
	
	companion object : XmlSerializableCompanion<ModData>{
		override val szInfoClass = ModData::class
		override fun XmlSzInfoBuilder<ModData>.buildSzInfo() {
			name = "mod"
			attribute(ModData::name)
			attribute(ModData::version)
			wrappedElements("state", "var", ModData::stateVars)
			elements("script", ModData::scripts)
			handleElement("hook") { _, attrs, input ->
				val mod = this
				content.add(XcScene().also { s ->
					val trigger = TimedTrigger()
					s.trigger = trigger
					for ((k,v) in attrs) when(k) {
						"type" -> trigger.type = when(v) {
							"daily" -> TimedTrigger.Type.DAILY
							"hourly" -> TimedTrigger.Type.HOURLY
							else -> kotlin.error("Unexpected hook@type=$v")
						}
						"name" -> s.name = v
						else -> kotlin.error("Unexpected hook@$k")
					}
					val contentLoader = XContentContainer.getSerializationInfo()
					input.forEachElement { tag, attrs ->
						contentLoader.deserializeElementInto(s,input,tag,attrs)
					}
					s.owner = mod
				})
			}
			elements("monster", ModData::monsters)
			handleElement("encounter") { _, attrs, input ->
				val mod = this
				lateinit var scene: XcScene
				val trigger = EncounterTrigger()
				input.forEachElement { tag2, attrs2 ->
					when (tag2) {
						"condition" -> trigger.condition = text()
						"chance" -> trigger.chance = text()
						"scene" -> {
							scene = XcScene.getSerializationInfo().deserialize(input,attrs2,mod)
						}
						else -> error("Unexpected encounter.$tag2")
					}
				}
				for ((k,v) in attrs) when(k) {
					"pool" -> trigger.pool = v
					"name" -> scene.name = v
					else -> error("Unexpected encounter@$k")
				}
				scene.trigger = trigger
				scene.owner = mod
				content.add(scene)
			}
			elementsByTag(ModData::content,
			              "scene" to XcScene::class,
			              "lib" to XcLib::class,
			              "text" to XcLib::class)
		}
		
		fun loadMod(src: InputStream):ModData {
			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
		}
		fun loadMod(src: Reader):ModData {
			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
		}
		fun saveMod(mod:ModData,dst: Writer) {
			getSerializationInfo().serializeDocument(mod, XmlBuilder(dst))
		}
		
	}
	
}

val DefaultModData by lazy {
	ModData.loadMod(ModData::class.java.getResourceAsStream("default.xml"))
}
