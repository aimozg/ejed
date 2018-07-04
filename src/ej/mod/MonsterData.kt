package ej.mod

import ej.editor.utils.*
import ej.utils.ValidateNonBlank
import ej.utils.ValidateUnique
import ej.utils.affix
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import java.io.StringWriter
import javax.xml.bind.Marshaller
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*

@XmlRootElement(name="monster")
class MonsterData : Patchable, ModDataNode {
	
	val idProperty = SimpleStringProperty()
	@ValidateNonBlank
	@ValidateUnique
	@get:XmlAttribute
	@PatchNoMerge
	var id: String by idProperty
	
	val baseIdProperty = SimpleStringProperty()
	@get:XmlAttribute(name = "base")
	@PatchNoMerge
	var baseId by baseIdProperty
	
	val nameProperty = SimpleStringProperty()
	@get:XmlElement
	var name by nameProperty
	
	@XmlElement(name="desc")
	private var descRaw: MonsterDesc? = null
	@XmlTransient
	val desc = MonsterDesc()
	
	val pluralProperty = SimpleBooleanProperty()
	@get:XmlElement
	var plural by pluralProperty
	
	val articleProperty = SimpleStringProperty()
	@get:XmlElement(name = "a")
	var article by articleProperty
	
	@XmlElement(name = "pronouns")
	private var pronounsRaw: Pronouns? = null
	@XmlTransient
	val pronouns = Pronouns()
	
	@XmlElement(name="body")
	private var bodyRaw: MonsterBodyData? = null
	@XmlTransient
	val body = MonsterBodyData()
	
	@XmlElement(name="combat")
	private var combatRaw: MonsterCombatData? = null
	@XmlTransient
	val combat = MonsterCombatData()
	
	@XmlElement(name = "script")
	private var scriptRaw: ModScript? = null
	@XmlTransient
	val script = ModScript()
	
	@XmlRootElement(name="desc")
	class MonsterDesc : XContentContainer("desc")
	
	class Pronouns : Patchable {
		@get:XmlAttribute
		var he: String = "he"
		@get:XmlAttribute
		var his: String = "his"
		@get:XmlAttribute
		var him: String = "him"
	}
	
	override fun toString() = "<monster id='$id'" +baseId.affix(" base='", "'") +"> " +name.affix(" <name>", "</name>") +" </monster>"
	
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
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		pronouns.applyPatch(pronounsRaw)
		body.applyPatch(bodyRaw)
		combat.applyPatch(combatRaw)
		desc.content.addAll(descRaw?.content?: emptyList())
	}
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun beforeMarshal(marshaller: Marshaller) {
		pronounsRaw = pronouns.takeIf { it.hasNotNulls() }
		bodyRaw = body.takeIf { it.hasNotNulls() }
		combatRaw = combat.takeIf { it.hasNotNulls() }
		descRaw = desc.takeIf { it.content.isNotEmpty() }
	}
}

val DefaultMonsterData by lazy {
	DefaultModData.monsters.find { it.id == "default" } ?: kotlin.error("Unable to load default monster data")
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
	
	val levelProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var level by levelProperty
	
	val strProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var str by strProperty
	
	val touProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var tou by touProperty
	
	val speProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var spe by speProperty
	
	val intProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var int by intProperty
	
	val wisProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var wis by wisProperty
	
	val libProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var lib by libProperty
	
	val senProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var sen by senProperty
	
	val corProperty = SimpleObjectProperty<Int>()
	@get:XmlElement
	var cor by corProperty
	
	val bonusHpProperty = SimpleObjectProperty<Int>()
	@get:XmlElement(name="bonusHP")
	var bonusHp by bonusHpProperty

	@XmlElement(name="weapon")
	private var weaponRaw: WeaponData? = null
	
	@XmlElement(name="armor")
	private var armorRaw: ArmorData? = null
	
	@XmlElement(name="loot")
	private var loot: LootData? = null
	
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