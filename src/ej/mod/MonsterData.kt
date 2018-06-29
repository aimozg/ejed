package ej.mod

import ej.editor.utils.PatchNoMerge
import ej.editor.utils.Patchable
import ej.editor.utils.spawnCopy
import ej.editor.utils.spawnPatchedCopy
import ej.utils.ValidateNonBlank
import ej.utils.ValidateUnique
import ej.utils.affix
import java.io.StringWriter
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlValue

@XmlRootElement(name="monster")
class MonsterData : Patchable {
	
	@ValidateNonBlank
	@ValidateUnique
	@get:XmlAttribute
	@PatchNoMerge
	var id: String? = null
	
	@get:XmlAttribute(name = "base")
	@PatchNoMerge
	var baseId: String? = null
	
	@get:XmlElement
	var name: String? = null
	
	@get:XmlElement
	var desc: MonsterDesc? = null
	
	@get:XmlElement
	var plural: Boolean? = null
	
	@get:XmlElement(name = "a")
	var article: String? = null
	
	@get:XmlElement(name = "pronouns")
	var pronouns: Pronouns? = null
	
	@get:XmlElement
	var body: MonsterBodyData? = null
	
	@get:XmlElement
	var combat: MonsterCombatData? = null
	
	@get:XmlElement(name = "script")
	var script: ModScript? = null
	
	class MonsterDesc : XContentContainer("desc")
	
	class Pronouns {
		@get:XmlAttribute
		var he: String = "he"
		@get:XmlAttribute
		var his: String = "his"
		@get:XmlAttribute
		var him: String = "him"
	}
	
	override fun toString() = "<monster id='$id'" +
			baseId.affix(" base='", "'") +
			"> " +
			name.affix(" <name>", "</name>") +
			" </monster>"
	
	fun patchedCopy(baseProvider:(id:String)->MonsterData?):MonsterData {
		return spawnCopy().applyPatch(baseProvider)
	}
	fun applyPatch(baseProvider:(id:String)->MonsterData?):MonsterData {
		val base = findBase(baseProvider)
		return spawnPatchedCopy(this, base?.applyPatch(baseProvider)?:DefaultMonsterData)
	}
	
	fun findBase(baseProvider: (id: String) -> MonsterData?) = baseId?.let(baseProvider)
	
	fun toXML() = StringWriter().also { writer ->
		ModData.jaxbContext.createMarshaller().marshal(this, writer)
	}.buffer.toString()
}

val DefaultMonsterData by lazy {
	DefaultModData.monsters.find { it.id == "default" } ?: error("Unable to load default monster data")
}

class MonsterBodyData : Patchable {
	
	@get:XmlElement
	var vagina: VaginaData? = null
	
	@get:XmlElement(name = "breasts")
	val penises: MutableList<PenisData> = ArrayList()
	
	@get:XmlElement(name = "balls")
	var balls: TesticleData? = null
	
	@get:XmlElement(name = "breasts")
	val breastRows: MutableList<BreastData> = ArrayList()
	
	@get:XmlElement(name = "anal")
	var anal: AnalData? = null
	
	@get:XmlElement(name = "height")
	var heightName: String? = null
	
	@get:XmlElement(name = "hips")
	var hipsName: String? = null
	
	@get:XmlElement(name = "butt")
	var buttName: String? = null
	
	@get:XmlElement(name = "skin")
	var skin: SkinData? = null
	
	@get:XmlElement(name = "hair")
	var hair: HairData? = null
	
	@get:XmlElement(name = "antennae")
	var antennaeName: String? = null
	
	@get:XmlElement(name = "arms")
	var armsName: String? = null
	
	@get:XmlElement(name = "beard")
	var beard: BeardData? = null
	
	@get:XmlElement(name = "claws")
	var claws: ClawsData? = null
	
	@get:XmlElement(name = "ears")
	var earsName: String? = null
	
	@get:XmlElement(name = "eyes")
	var eyes: EyesData? = null
	
	@get:XmlElement(name = "face")
	var faceName: String? = null
	
	@get:XmlElement(name = "gills")
	var gillsName: String? = null
	
	@get:XmlElement(name = "horns")
	var horns: HornsData? = null
	
	@get:XmlElement(name = "legs")
	var legs: LegsData? = null
	
	@get:XmlElement(name = "rearBody")
	var rearBodyName: String? = null
	
	@get:XmlElement(name = "tail")
	var tail: TailData? = null
	
	@get:XmlElement(name = "tongue")
	var tongueName: String? = null
	
	@get:XmlElement(name = "wings")
	var wingsName: String? = null
	
	class VaginaData : Patchable {
		@get:XmlAttribute
		var virgin: Boolean? = null
		@get:XmlAttribute(name="wetness")
		var wetnessName: String? = null
		@get:XmlAttribute(name="looseness")
		var loosenessName: String? = null
	}
	
	class PenisData : Patchable {
		@get:XmlAttribute(name="length")
		var lengthName: String? = null
		@get:XmlAttribute(name="thickness")
		var thicknessName: String? = null
		@get:XmlAttribute(name="type")
		var typeName: String? = null
	}
	
	class TesticleData : Patchable {
		@get:XmlAttribute(name="count")
		var count:Int? = null
		@get:XmlAttribute(name="size")
		var sizeName:String? = null
	}
	
	class BreastData : Patchable {
		@get:XmlValue
		var sizeName: String = "flat"
	}
	
	class AnalData : Patchable {
		@get:XmlAttribute
		var looseness: String? = null
		@get:XmlAttribute
		var wetness: String? = null
	}
	
	class SkinData : Patchable {
		@get:XmlAttribute(name = "coverage")
		var coverageName: String? = null
		@get:XmlElement
		var base: SkinLayerData? = null
		@get:XmlElement
		var coat: SkinLayerData? = null
	}
	
	class SkinLayerData : Patchable {
		@get:XmlAttribute(name = "type")
		var typeName: String? = null
		
		@get:XmlAttribute
		var color: String? = null
		
		@get:XmlAttribute
		var color2: String? = null
		
		@get:XmlAttribute(name = "pattern")
		var patternName: String? = null
		
		@get:XmlAttribute
		var adj: String? = null
		
		@get:XmlAttribute
		var desc: String? = null
	}
	
	class HairData : Patchable {
		@get:XmlAttribute(name="length")
		var lengthName:String? = null
		@get:XmlAttribute(name="color")
		var color:String? = null
		@get:XmlAttribute(name="type")
		var typeName:String? = null
	}
	
	class BeardData : Patchable {
		
		@get:XmlAttribute(name="style")
		var styleName: String? = null
		
		@get:XmlAttribute(name="length")
		var lengthName: String? = null
	}
	
	class ClawsData : Patchable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="color")
		var color: String? = null
	}
	
	class EyesData : Patchable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
		
		@get:XmlAttribute(name="color")
		var color: String? = null
	}
	
	class HornsData : Patchable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
	
	class LegsData : Patchable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
	
	class TailData : Patchable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
}

class MonsterCombatData : Patchable {
	
	@get:XmlElement
	var level: Int? = null
	
	@get:XmlElement
	var str: Int? = null
	
	@get:XmlElement
	var tou: Int? = null
	
	@get:XmlElement
	var spe: Int? = null
	
	@get:XmlElement
	var int: Int? = null
	
	@get:XmlElement
	var wis: Int? = null
	
	@get:XmlElement
	var lib: Int? = null
	
	@get:XmlElement
	var sen: Int? = null
	
	@get:XmlElement
	var cor: Int? = null
	
	@get:XmlElement
	var bonusHP: Int? = null
	
	@get:XmlElement
	var weapon: WeaponData? = null
	
	@get:XmlElement
	var armor: ArmorData? = null
	
	@get:XmlElement
	var loot: LootData? = null
	
	class WeaponData {
		// TODO
	}
	
	class ArmorData {
		// TODO
	}
	
	class LootData {
		@get:XmlElement
		var gems: GemsData? = null
		
		@get:XmlElement(name = "item")
		val items: MutableList<LootItemData> = ArrayList()
		
		class GemsData {
			@get:XmlAttribute
			var min: Int? = null
			@get:XmlAttribute
			var max: Int? = null
			@get:XmlAttribute
			var value: Int? = null
		}
		
		class LootItemData {
			@get:XmlAttribute
			var weight: Double? = null
			
			@get:XmlValue
			var itemid: String? = null
		}
	}
}