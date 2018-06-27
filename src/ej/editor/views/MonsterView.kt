package ej.editor.views

import ej.editor.AModFragment
import ej.editor.AModView
import ej.editor.Styles
import ej.editor.utils.colspan
import ej.editor.utils.smartRow
import javafx.geometry.Pos
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

class MonsterView: AModView("Monster") {
	init {
		
		headingProperty.bind(controller.monsterProperty.stringBinding {
			if (it == null) "Monster" else {
				"Monster ${it.id}" +(if(it.name==it.id) "" else " (${it.name?:"unnamed"})")
			}
		})
	}
	
	override val root = tabpane {
		tab(MonsterBasicView::class)
		tab(MonsterBodyView::class)
		tab(MonsterDescView::class)
		tab(MonsterSpecialView::class)
		tab(MonsterScriptsView::class)
		tab(MonsterScenesView::class)
		connectWorkspaceActions()
	}
}

class MonsterBasicView:AModFragment("Basic") {
	override val root = vbox {
		val vm = controller.monsterVM
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

class MonsterBodyView:AModFragment("Body") {
	override val root = form {
		label("Body")
		// TODO
	}
}

class MonsterDescView:AModFragment("Description") {
	override val root = form {
		val vm = controller.monsterVM
		fieldset("Description") {
			textarea(vm.descSource) {
				isWrapText = true
				this.prefHeightProperty().bind(this@form.heightProperty())
			}
		}
	}
}

class MonsterSpecialView:AModFragment("Specials") {
	override val root = form {
		label("Specials")
		// TODO
	}
}

class MonsterScriptsView:AModFragment("Scripts") {
	override val root = form {
		label("Scripts")
		// TODO
	}
}

class MonsterScenesView:AModFragment("Scenes") {
	override val root = form {
		label("Scenes")
		// TODO
	}
}