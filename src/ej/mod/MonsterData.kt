package ej.mod

import ej.utils.ValidateNonBlank
import ej.utils.ValidateUnique
import ej.utils.affix
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlValue

class MonsterData {
	
	@ValidateNonBlank
	@ValidateUnique
	@get:XmlAttribute
	var id:String = ""
	
	@get:XmlAttribute(name="base")
	var baseId:String? = null
	
	@get:XmlElement
	var name:String? = null
	
	@get:XmlElement
	var desc:MonsterDesc? = null
	
	class MonsterDesc: XContentContainer("desc")
	
	@get:XmlElement
	var plural:Boolean? = null
	
	@get:XmlElement(name="a")
	var article:String? = null
	
	@get:XmlElement(name="pronouns")
	var pronouns: Pronouns? = null
	
	@get:XmlElement
	var body: MonsterBodyData? = null
	
	@get:XmlElement
	var combat: MonsterCombatData? = null
	
	@get:XmlElement(name="script")
	var script: ModScript? = null
	
	class Pronouns {
		@get:XmlAttribute
		var he:String = "he"
		@get:XmlAttribute
		var his:String = "his"
		@get:XmlAttribute
		var him:String = "him"
	}
	
	override fun toString() = "<monster id='$id'" +
			baseId.affix(" base='","'") +
			"> " +
			name.affix(" <name>","</name>") +
			" </monster>"
}




class MonsterBodyData {
	
	@get:XmlElement
	var vagina: VaginaData? = null
	
	// TODO p0n0s
	// TODO balls
	
	@get:XmlElement(name = "breasts")
	val breastRows: MutableList<BreastData> = ArrayList()
	
	@get:XmlElement(name = "anal")
	var anal: AnalData? = null
	
	@get:XmlElement(name="height")
	var heightName:String? = null
	
	@get:XmlElement(name="hips")
	var hipsName:String? = null
	
	@get:XmlElement(name="butt")
	var buttName:String? = null
	
	@get:XmlElement(name="skin")
	var skin:SkinData? = null
	
	@get:XmlElement(name="hair")
	var hair:HairData? = null

	@get:XmlElement(name="antennae")
	var antennaeName:String? = null

	@get:XmlElement(name="arms")
	var armsName:String? = null
	
	@get:XmlElement(name="beard")
	var beard:BeardData? = null
	
	@get:XmlElement(name="claws")
	var claws:ClawsData? = null
	
	@get:XmlElement(name="ears")
	var earsName:String? = null
	
	@get:XmlElement(name="eyes")
	var eyes:EyesData? = null
	
	@get:XmlElement(name="face")
	var faceName:String? = null
	
	@get:XmlElement(name="gills")
	var gillsName:String? = null
	
	@get:XmlElement(name="horns")
	var horns:HornsData? = null
	
	@get:XmlElement(name="legs")
	var legs:LegsData? = null
	
	@get:XmlElement(name="rearBody")
	var rearBodyName:String? = null
	
	@get:XmlElement(name="tail")
	var tail:TailData? = null
	
	@get:XmlElement(name="tongue")
	var tongueName:String? = null
	
	@get:XmlElement(name="wings")
	var wingsName:String? = null
	
	class VaginaData {
		@get:XmlAttribute
		var virgin:Boolean? = null
		@get:XmlAttribute
		var wetness:String? = null
		@get:XmlAttribute
		var looseness:String? = null
	}
	
	class BreastData {
		@get:XmlValue
		var sizeName:String = "flat"
	}
	
	class AnalData {
		@get:XmlAttribute
		var looseness:String? = null
		@get:XmlAttribute
		var wetness:String? = null
	}
	
	class SkinData {
		@get:XmlAttribute(name="coverage")
		var coverageName:String? = null
		@get:XmlElement
		var base:SkinLayerData? = null
		@get:XmlElement
		var coat:SkinLayerData? = null
	}
	
	class SkinLayerData {
		@get:XmlAttribute(name="type")
		var typeName:String? = null
		
		@get:XmlAttribute
		var color:String? = null
		
		@get:XmlAttribute
		var color2:String? = null
		
		@get:XmlAttribute (name="pattern")
		var patternName:String? = null
		
		@get:XmlAttribute
		var adj:String? = null
		
		@get:XmlAttribute
		var desc:String? = null
	}
	
	class HairData {
		// TODO
	}
	class BeardData {
		// TODO
	}
	class ClawsData {
		// TODO
	}
	class EyesData {
		// TODO
	}
	class HornsData {
		// TODO
	}
	class LegsData {
		// TODO
	}
	class TailData {
		// TODO
	}
}

class MonsterCombatData {
	
	@get:XmlElement
	var level:Int? = null
	
	@get:XmlElement
	var str:Int? = null
	
	@get:XmlElement
	var tou:Int? = null
	
	@get:XmlElement
	var spe:Int? = null
	
	@get:XmlElement
	var int:Int? = null
	
	@get:XmlElement
	var wis:Int? = null
	
	@get:XmlElement
	var lib:Int? = null
	
	@get:XmlElement
	var sen:Int? = null
	
	@get:XmlElement
	var cor:Int? = null
	
	@get:XmlElement
	var bonusHP:Int? = null
	
	@get:XmlElement
	var weapon:WeaponData? = null
	
	@get:XmlElement
	var armor:ArmorData? = null
	
	@get:XmlElement
	var loot:LootData? = null
	
	class WeaponData {
		// TODO
	}
	class ArmorData {
		// TODO
	}
	class LootData {
		@get:XmlElement
		var gems:GemsData? = null
		
		@get:XmlElement(name="item")
		val items:MutableList<LootItemData> = ArrayList()
		
		class GemsData {
			@get:XmlAttribute
			var min:Int? = null
			@get:XmlAttribute
			var max:Int? = null
			@get:XmlAttribute
			var value:Int? = null
		}
		
		class LootItemData {
			@get:XmlAttribute
			var weight:Double? = null
			
			@get:XmlValue
			var itemid:String? = null
		}
	}
}