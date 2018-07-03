package ej.editor.views

import ej.editor.AModView
import ej.editor.MonsterViewModel
import ej.editor.Styles
import ej.editor.isDifferent
import ej.editor.utils.colspan
import ej.editor.utils.smartRow
import javafx.geometry.Pos
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class MonsterView(val monsterVM:MonsterViewModel): AModView() {
	
	override fun onSave() {
		for (property in monsterVM.propertyMap.keys) {
			if (property.isDifferent) println("${property.name} is different = ${property.value}")
		}
		println("---")
		println("complete:")
		println(monsterVM.item.toXML())
		println("as patch:")
		println(monsterVM.toPatch()?.toXML())
	}
	
	override val root = tabpane {
		tab<MonsterBasicView>()
		tab<MonsterBodyView>()
		tab<MonsterDescView>()
		tab<MonsterSpecialView>()
		tab<MonsterScriptsView>()
		tab<MonsterScenesView>()
	}
}

class MonsterBasicView():AModView("Basic") {
	val view = find<MonsterView>()
	override val root = vbox {
		val vm = view.monsterVM
		hbox(10) {
			form {
				spacing = 10.0
				fieldset("Core") {
					field("ID") { textfield(vm.id) }
					field("Prorotype monster") {
						textfield(vm.baseId) // TODO select from other monsters
					}
					field("Name") { textfield(vm.name) }
				}
				fieldset("Grammar") {
					field("Group") {
						checkbox("(use plural form)", vm.plural)
					}
					field("Article") { textfield(vm.article) }
					label("Pronouns:")
					field("'he'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(vm.pronounHe)
					}
					field("'his'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(vm.pronounHis)
					}
					field("'him'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(vm.pronounHim)
					}
				}
			}
			form {
				addClass(Styles.monsterCombat)
				gridpane {
					row {
						label("Combat") {
							addClass(Stylesheet.legend)
							colspan(6)
						}
					}
					smartRow {
						vbox {
							colspan(2)
							label("Level") { useMaxWidth = true }
							textfield(vm.combat.level, IntegerStringConverter()){ prefColumnCount = 3 }
						}
					}
					row {
						vbox {
							label("STR") { useMaxWidth = true }
							textfield(vm.combat.str, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("TOU") { useMaxWidth = true }
							textfield(vm.combat.tou, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("SPE") { useMaxWidth = true }
							textfield(vm.combat.spe, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("INT") { useMaxWidth = true }
							textfield(vm.combat.int, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("WIS") { useMaxWidth = true }
							textfield(vm.combat.wis, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("LIB") { useMaxWidth = true }
							textfield(vm.combat.lib, IntegerStringConverter()) { prefColumnCount = 3 }
						}
					}
					smartRow {
						vbox {
							colspan(2)
							label("Sensitivity") { useMaxWidth = true }
							textfield(vm.combat.sen, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							colspan(2)
							label("Corruption") { useMaxWidth = true }
							textfield(vm.combat.cor, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							colspan(2)
							label("Bonus HP") { useMaxWidth = true }
							textfield(vm.combat.bonusHP, IntegerStringConverter()) { prefColumnCount = 3 }
						}
					}
				}
			}
		}
		
	}
}

class MonsterBodyView():AModView("Body") {
	val view = find<MonsterView>()
	override val root = form {
		label("Body")
		// TODO
	}
}

class MonsterDescView():AModView("Description") {
	val view = find<MonsterView>()
	val editor = XStatementTreeWithEditor()
	
	override val root = vbox {
		add(editor)
		editor.contents = view.monsterVM.desc.value.content
	}
}

class MonsterSpecialView():AModView("Specials") {
	val view = find<MonsterView>()
	override val root = form {
		label("Specials")
		// TODO
	}
}

class MonsterScriptsView():AModView("Scripts") {
	val view = find<MonsterView>()
	override val root = form {
		label("Scripts")
		// TODO
	}
}

class MonsterScenesView():AModView("Scenes") {
	val view = find<MonsterView>()
	override val root = form {
		label("Scenes")
		// TODO
	}
}