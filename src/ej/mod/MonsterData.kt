package ej.mod

import ej.editor.utils.PatchNoMerge
import ej.editor.utils.Patchable
import ej.editor.utils.spawnCopy
import ej.editor.utils.spawnPatchedCopy
import ej.utils.affix
import ej.xml.*
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

class MonsterData : Patchable, ModDataNode, XmlSerializable {
	
	val idProperty = SimpleStringProperty("")
	@PatchNoMerge
	var id: String by idProperty
	
	val baseIdProperty = SimpleStringProperty()
	@PatchNoMerge
	var baseId: String? by baseIdProperty
	
	val nameProperty = SimpleStringProperty()
	var name: String? by nameProperty
	
	val desc = MonsterDesc()
	
	val pluralProperty = SimpleBooleanProperty()
	var plural by pluralProperty
	
	val articleProperty = SimpleStringProperty()
	var article: String? by articleProperty
	
	val body = MonsterBodyData()
	
	val combat = MonsterCombatData()
	
	val script = ModScript()
	
	class MonsterDesc : XContentContainer() {
		companion object : XmlSerializableCompanion<MonsterDesc> {
			override val szInfoClass = MonsterDesc::class
			override fun XmlSzInfoBuilder<MonsterDesc>.buildSzInfo() {
				inherit(XContentContainer)
				name = "desc"
			}
		}
	}
	
	val pronounHeProperty = SimpleObjectProperty<String?>()
	var pronounHe: String? by pronounHeProperty
	val pronounHisProperty = SimpleObjectProperty<String?>()
	var pronounHis: String? by pronounHisProperty
	val pronounHimProperty = SimpleObjectProperty<String?>()
	var pronounHim: String? by pronounHimProperty
	
	override fun toString() = "<monster id='$id'" + baseId.affix(" base='", "'") + "> " + name.affix(" <name>",
	                                                                                                 "</name>") + " </monster>"
	
	fun patchedCopy(baseProvider: (id: String) -> MonsterData?): MonsterData {
		return spawnCopy().applyPatch(baseProvider)
	}
	
	fun applyPatch(baseProvider: (id: String) -> MonsterData?): MonsterData {
		val base = findBase(baseProvider)
		return spawnPatchedCopy(this, base?.applyPatch(baseProvider) ?: DefaultMonsterData)
	}
	
	fun findBase(baseProvider: (id: String) -> MonsterData?) = baseId?.let(baseProvider)
	
	companion object : XmlSerializableCompanion<MonsterData> {
		override val szInfoClass = MonsterData::class
		
		override fun XmlSzInfoBuilder<MonsterData>.buildSzInfo() {
			attribute(MonsterData::id)
			attribute(MonsterData::baseId, "base")
			element(MonsterData::name)
			elementOverwrite(MonsterData::desc)
			element(MonsterData::plural)
			element(MonsterData::article, "a")
			element(MonsterData::pronounHe, "he")
			element(MonsterData::pronounHim, "him")
			element(MonsterData::pronounHis, "his")
			handleElement("pronouns") { _, _, input ->
				input.forEachElement { tag, _ ->
					when (tag) {
						"he" -> pronounHe = text()
						"him" -> pronounHim = text()
						"his" -> pronounHis = text()
						else -> error("unknown monster.pronouns.$tag")
					}
				}
			}
			elementOverwrite(MonsterData::body)
			elementOverwrite(MonsterData::combat)
			elementOverwrite(MonsterData::script)
			
		}
		
	}
}

val DefaultMonsterData by lazy {
	DefaultModData.monsters.find { it.id == "default" } ?: kotlin.error("Unable to load default monster data")
}

class MonsterBodyData : Patchable, XmlAutoSerializable {
	
	@Element
	var vagina: VaginaData? = null
	
	@Elements("penis")
	val penises: MutableList<PenisData> = ArrayList()
	
	@Element("balls")
	var balls: TesticleData? = null
	
	@Elements("breasts")
	val breastRows: MutableList<BreastData> = ArrayList()
	
	@Element("anal")
	var anal: AnalData? = null
	
	@Element("height")
	var heightName: String? = null
	
	@Element("hips")
	var hipsName: String? = null
	
	@Element("butt")
	var buttName: String? = null
	
	@Element("skin")
	var skin: SkinData? = null
	
	@Element("hair")
	var hair: HairData? = null
	
	@Element("antennae")
	var antennaeName: String? = null
	
	@Element("arms")
	var armsName: String? = null
	
	@Element("beard")
	var beard: BeardData? = null
	
	@Element("claws")
	var claws: ClawsData? = null
	
	@Element("ears")
	var earsName: String? = null
	
	@Element("eyes")
	var eyes: EyesData? = null
	
	@Element("face")
	var faceName: String? = null
	
	@Element("gills")
	var gillsName: String? = null
	
	@Element("horns")
	var horns: HornsData? = null
	
	@Element("legs")
	var legs: LegsData? = null
	
	@Element("rearBody")
	var rearBodyName: String? = null
	
	@Element("tail")
	var tail: TailData? = null
	
	@Element("tongue")
	var tongueName: String? = null
	
	@Element("wings")
	var wingsName: String? = null
	
	class VaginaData : Patchable, XmlAutoSerializable {
		@Attribute
		var virgin: Boolean? = null
		@Attribute("wetness")
		var wetnessName: String? = null
		@Attribute("looseness")
		var loosenessName: String? = null
	}
	
	class PenisData : Patchable, XmlAutoSerializable {
		@Attribute("length")
		var lengthName: String? = null
		@Attribute("thickness")
		var thicknessName: String? = null
		@Attribute("type")
		var typeName: String? = null
	}
	
	class TesticleData : Patchable, XmlAutoSerializable {
		@Attribute("count")
		var count: Int? = null
		@Attribute("size")
		var sizeName: String? = null
	}
	
	class BreastData : Patchable, XmlAutoSerializable {
		@TextBody
		var sizeName: String = "flat"
	}
	
	class AnalData : Patchable, XmlAutoSerializable {
		@Attribute
		var looseness: String? = null
		@Attribute
		var wetness: String? = null
	}
	
	class SkinData : Patchable, XmlAutoSerializable {
		@Attribute("coverage")
		var coverageName: String? = null
		@Element
		var base: SkinLayerData? = null
		@Element
		var coat: SkinLayerData? = null
	}
	
	class SkinLayerData : Patchable, XmlAutoSerializable {
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute
		var color: String? = null
		
		@Attribute
		var color2: String? = null
		
		@Attribute("pattern")
		var patternName: String? = null
		
		@Attribute
		var adj: String? = null
		
		@Attribute
		var desc: String? = null
	}
	
	class HairData : Patchable, XmlAutoSerializable {
		@Attribute("length")
		var lengthName: String? = null
		@Attribute("color")
		var color: String? = null
		@Attribute("type")
		var typeName: String? = null
	}
	
	class BeardData : Patchable, XmlAutoSerializable {
		
		@Attribute("style")
		var styleName: String? = null
		
		@Attribute("length")
		var lengthName: String? = null
	}
	
	class ClawsData : Patchable, XmlAutoSerializable {
		
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute("color")
		var color: String? = null
	}
	
	class EyesData : Patchable, XmlAutoSerializable {
		
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute("count")
		var count: Int? = null
		
		@Attribute("color")
		var color: String? = null
	}
	
	class HornsData : Patchable, XmlAutoSerializable {
		
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute("count")
		var count: Int? = null
	}
	
	class LegsData : Patchable, XmlAutoSerializable {
		
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute("count")
		var count: Int? = null
	}
	
	class TailData : Patchable, XmlAutoSerializable {
		
		@Attribute("type")
		var typeName: String? = null
		
		@Attribute("count")
		var count: Int? = null
	}
}

class MonsterCombatData : Patchable, XmlAutoSerializable {
	
	val levelProperty = SimpleObjectProperty<Int>()
	@Element
	var level: Int? by levelProperty
	
	val strProperty = SimpleObjectProperty<Int>()
	@Element
	var str: Int? by strProperty
	
	val touProperty = SimpleObjectProperty<Int>()
	@Element
	var tou: Int? by touProperty
	
	val speProperty = SimpleObjectProperty<Int>()
	@Element
	var spe: Int? by speProperty
	
	val intProperty = SimpleObjectProperty<Int>()
	@Element
	var int: Int? by intProperty
	
	val wisProperty = SimpleObjectProperty<Int>()
	@Element
	var wis: Int? by wisProperty
	
	val libProperty = SimpleObjectProperty<Int>()
	@Element
	var lib: Int? by libProperty
	
	val senProperty = SimpleObjectProperty<Int>()
	@Element
	var sen: Int? by senProperty
	
	val corProperty = SimpleObjectProperty<Int>()
	@Element
	var cor: Int? by corProperty
	
	val bonusHpProperty = SimpleObjectProperty<Int>()
	@Element("bonusHP")
	var bonusHp: Int? by bonusHpProperty
	
	@Element
	var weapon: WeaponData? = null
	
	@Element
	var armor: ArmorData? = null
	
	@Element
	var loot: LootData? = null
	
	class WeaponData : XmlAutoSerializable {
		// TODO
	}
	
	class ArmorData : XmlAutoSerializable {
		// TODO
	}
	
	class LootData : XmlAutoSerializable {
		@Element
		var gems: GemsData? = null
		
		@Elements("item")
		val items: MutableList<LootItemData> = ArrayList()
		
		class GemsData : XmlAutoSerializable {
			@Attribute
			var min: Int? = null
			@Attribute
			var max: Int? = null
			@Attribute
			var value: Int? = null
		}
		
		class LootItemData : XmlAutoSerializable {
			@Attribute
			var weight: Double? = null
			
			@TextBody
			var itemid: String? = null
		}
	}
}