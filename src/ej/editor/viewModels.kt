package ej.editor

import ej.editor.utils.spawnCopy
import ej.editor.utils.toPatch
import ej.mod.DefaultMonsterData
import ej.mod.ModData
import ej.mod.MonsterCombatData
import ej.mod.MonsterData
import javafx.beans.property.Property
import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */

class ModViewModel(property: Property<ModData>) : ItemViewModel<ModData>() {
	val name = bind { SimpleStringProperty(item?.name?:"") }
	val monsters = bind {
		SimpleListProperty((item?.monsters?: ArrayList()).observable())
	}
	val findMonster: (String) -> MonsterData? = { id:String ->
		monsters.value.find { it.id == id }
	}
	
	override fun onCommit(commits: List<Commit>) {
		val changedSet = commits.mapNotNullTo(HashSet()) { if (it.changed) it.property else null }
		if (name in changedSet) {
			item.name = name.value
		}
		if (monsters in changedSet) {
			println("mod.monsters ${item.monsters} -> ${monsters.value}")
			item.monsters.clear()
			item.monsters.addAll(monsters.value)
		}
	}
	
	init {
		itemProperty.bind(property)
	}
}

val<T> Property<T>.isDifferent:Boolean get() = (bean as? MonsterViewModel)?.isPropertyDifferent(this) ?: false

open class MonsterViewModelBase(val mod:ModViewModel, monster: MonsterData):ItemViewModel<MonsterData>(monster) {
	
	val id = bind(MonsterData::id)
	val baseId = bind(MonsterData::baseId)
	val name = bind(MonsterData::name)
	val descSource = bind(MonsterData::desc).stringBinding { it?.innerXML() }
	val plural = bind(MonsterData::plural)
	val article = bind(MonsterData::article)
	val pronounHe = bind { item.pronouns?.observable(MonsterData.Pronouns::he) }
	val pronounHis = bind { item.pronouns?.observable(MonsterData.Pronouns::his) }
	val pronounHim = bind { item.pronouns?.observable(MonsterData.Pronouns::him) }
	val body = bind(MonsterData::body)
	val combat = MonsterCombatDataViewModel(itemProperty)
	val script = bind(MonsterData::script)
}
class MonsterViewModel(mod:ModViewModel,monster:MonsterData):MonsterViewModelBase(mod,monster.patchedCopy(mod.findMonster) ) {
	val base = MonsterViewModelBase(mod,
	                                monster.findBase(mod.findMonster)?.patchedCopy(mod.findMonster)
			                                ?: DefaultMonsterData)
	
	fun<T> isPropertyDifferent(property:Property<T>):Boolean {
		val newValue = property.value
		val old = base.propertyMap.keys.find { it.name == property.name }?.value
		return old != newValue
	}
	fun toPatch():MonsterData? = item.spawnCopy().toPatch(base.item)
}

class MonsterCombatDataViewModel(monsterProperty:Property<MonsterData?>) : ItemViewModel<MonsterCombatData>() {
	val level = bind(MonsterCombatData::level)
	val str = bind(MonsterCombatData::str)
	val tou = bind(MonsterCombatData::tou)
	val spe = bind(MonsterCombatData::spe)
	val int = bind(MonsterCombatData::int)
	val wis = bind(MonsterCombatData::wis)
	val lib = bind(MonsterCombatData::lib)
	val sen = bind(MonsterCombatData::sen)
	val cor = bind(MonsterCombatData::cor)
	val bonusHP = bind(MonsterCombatData::bonusHP)
	val weapon = bind(MonsterCombatData::weapon)
	val armor = bind(MonsterCombatData::armor)
	val loot = bind(MonsterCombatData::loot)
	init {
		itemProperty.bind(monsterProperty.objectBinding{it?.combat})
	}
}
