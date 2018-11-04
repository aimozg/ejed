package ej.mod

import ej.xml.*
import javafx.beans.property.Property
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.ObservableList
import tornadofx.*
import java.io.File
import java.io.InputStream
import java.io.Reader
import java.io.Writer

/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

interface ModDataNode
interface XStatement : ModDataNode, XmlSerializable
interface XComplexStatement: XStatement {
	val content: ObservableList<XStatement>
}
interface StoryContainer : ModDataNode, XmlSerializable {
	val lib: ObservableList<StoryStmt>
	val owner: ModDataNode?
}
enum class ValidationStatus(val hasValid:Boolean,val hasInvalid:Boolean) {
	UNKNOWN(false,false),
	VALID(true,false),
	MIXED(true,true),
	INVALID(false,true);
	
	operator fun plus(other: ValidationStatus): ValidationStatus = when(this) {
		UNKNOWN -> other
		VALID -> if (other.hasInvalid) MIXED else VALID
		MIXED -> MIXED
		INVALID -> if (other.hasValid) MIXED else INVALID
	}
	operator fun times(other: ValidationStatus): ValidationStatus = when(this) {
		UNKNOWN -> other
		VALID -> if (other.hasInvalid) INVALID else VALID
		MIXED -> MIXED
		INVALID -> INVALID
	}
}
interface StoryStmt : StoryContainer {
	var name: String
	val nameProperty: Property<String>
	var isValid: ValidationStatus
	val isValidProperty: Property<ValidationStatus>
	val path:String get() = ownersToRoot().fold(name) { s, story ->
		"${story.name}/$s"
	}
}
val ModDataNode.acceptsMenu: Boolean
	get() = this is XcScene
val ModDataNode.acceptsActions: Boolean
	get() = acceptsMenu || this is XcNamedText

class ModData : StoryContainer, ModDataNode, XmlSerializable {
	override val owner: ModDataNode? = null
	
	var sourceFile: File? = null
	
	val nameProperty = SimpleStringProperty("")
	var name:String  by nameProperty
	
	val versionProperty = SimpleIntegerProperty(0)
	var version: Int by versionProperty
	
	val stateVars: ObservableList<StateVar> = ArrayList<StateVar>().observable()
	
	val scripts = ArrayList<ModScript>().observable()
	
	val monsters: ObservableList<MonsterData> = ArrayList<MonsterData>().observable()
	
	override val lib: ObservableList<StoryStmt> = ArrayList<StoryStmt>().observable()
	
	fun allStories() = sequence {
		val run = ArrayList(lib)
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
				lib.joinToString(" ")+
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
				lib.add(XcScene().also { s ->
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
					else -> kotlin.error("Unexpected encounter@$k")
				}
				scene.trigger = trigger
				scene.owner = mod
				lib.add(scene)
			}
			elementsByTag(ModData::lib,
			              "scene" to XcScene::class,
			              "lib" to XcLib::class,
			              "text" to XcNamedText::class)
		}
		
		fun loadMod(src: InputStream):ModData {
			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
		}
		fun loadMod(src: Reader):ModData {
			return getSerializationInfo().deserializeDocument(XmlExplorer(src))
		}
		fun saveMod(mod:ModData,dst: Writer) {
			val builder = XmlStreamBuilder(dst)
			getSerializationInfo().serializeDocument(mod, builder)
		}
		
	}
	
}

val DefaultModData by lazy {
	ModData.loadMod(ModData::class.java.getResourceAsStream("default.xml"))
}
