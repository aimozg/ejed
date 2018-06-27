package ej.mod

import ej.editor.utils.CopyAssignable
import ej.editor.utils.CopyAssignableNoMerge
import ej.utils.ValidateNonBlank
import ej.utils.ValidateUnique
import ej.utils.affix
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlValue

class MonsterData : CopyAssignable {
	
	@ValidateNonBlank
	@ValidateUnique
	@get:XmlAttribute
	@CopyAssignableNoMerge
	var id: String? = null
	
	@get:XmlAttribute(name = "base")
	@CopyAssignableNoMerge
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
	/*
	override fun assignFrom(other: MonsterData) {
		name = name ?: other.name
		desc = desc ?: other.desc?.spawnCopy()
		plural = plural ?: other.plural
		article = article ?: other.article
		pronouns = pronouns ?: other.pronouns?.spawnCopy()
		body = mergeAssign(body, other.body)
		combat = mergeAssign(combat, other.combat)
		script = mergeAssign(script, other.script)
	}
	
	override fun spawnNew() = MonsterData()
	*/
	
	override fun toString() = "<monster id='$id'" +
			baseId.affix(" base='", "'") +
			"> " +
			name.affix(" <name>", "</name>") +
			" </monster>"
}

val DefaultMonsterData by lazy {
	DefaultModData.monsters.find { it.id == "default" } ?: error("Unable to load default monster data")
}

class MonsterBodyData : CopyAssignable {
	
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
	
	class VaginaData : CopyAssignable {
		@get:XmlAttribute
		var virgin: Boolean? = null
		@get:XmlAttribute(name="wetness")
		var wetnessName: String? = null
		@get:XmlAttribute(name="looseness")
		var loosenessName: String? = null
	}
	
	class PenisData : CopyAssignable {
		@get:XmlAttribute(name="length")
		var lengthName: String? = null
		@get:XmlAttribute(name="thickness")
		var thicknessName: String? = null
		@get:XmlAttribute(name="type")
		var typeName: String? = null
	}
	
	class TesticleData : CopyAssignable {
		@get:XmlAttribute(name="count")
		var count:Int? = null
		@get:XmlAttribute(name="size")
		var sizeName:String? = null
	}
	
	class BreastData : CopyAssignable {
		@get:XmlValue
		var sizeName: String = "flat"
	}
	
	class AnalData : CopyAssignable {
		@get:XmlAttribute
		var looseness: String? = null
		@get:XmlAttribute
		var wetness: String? = null
	}
	
	class SkinData : CopyAssignable {
		@get:XmlAttribute(name = "coverage")
		var coverageName: String? = null
		@get:XmlElement
		var base: SkinLayerData? = null
		@get:XmlElement
		var coat: SkinLayerData? = null
	}
	
	class SkinLayerData : CopyAssignable {
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
	
	class HairData : CopyAssignable {
		@get:XmlAttribute(name="length")
		var lengthName:String? = null
		@get:XmlAttribute(name="color")
		var color:String? = null
		@get:XmlAttribute(name="type")
		var typeName:String? = null
	}
	
	class BeardData : CopyAssignable {
		
		@get:XmlAttribute(name="style")
		var styleName: String? = null
		
		@get:XmlAttribute(name="length")
		var lengthName: String? = null
	}
	
	class ClawsData : CopyAssignable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="color")
		var color: String? = null
	}
	
	class EyesData : CopyAssignable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
		
		@get:XmlAttribute(name="color")
		var color: String? = null
	}
	
	class HornsData : CopyAssignable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
	
	class LegsData : CopyAssignable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
	
	class TailData : CopyAssignable {
		
		@get:XmlAttribute(name="type")
		var typeName: String? = null
		
		@get:XmlAttribute(name="count")
		var count: Int? = null
	}
}

class MonsterCombatData : CopyAssignable {
	
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