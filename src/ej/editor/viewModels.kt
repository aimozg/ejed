package ej.editor

import ej.mod.ModData
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
		itemProperty.bindBidirectional(property)
	}
}

class MonsterViewModel(property: Property<MonsterData?>):ItemViewModel<MonsterData>() {
	
	val id = bind(MonsterData::id)
	val baseId = bind(MonsterData::baseId)
	val name = bind(MonsterData::name)
	val descSource = bind(MonsterData::desc).stringBinding { it?.innerXML()?:" "}
	val plural = bind(MonsterData::plural)
	val article = bind(MonsterData::article)
	val pronounHe = bind(MonsterData::pronouns).stringBinding { it?.he?:" "}
	val pronounHis = bind(MonsterData::pronouns).stringBinding { it?.his?:" "}
	val pronounHim = bind(MonsterData::pronouns).stringBinding { it?.him?:" "}
	val body = bind(MonsterData::body)
	val combat = bind(MonsterData::combat)
	val script = bind(MonsterData::script)
	
	
	
	init {
		itemProperty.bindBidirectional(property)
	}
}