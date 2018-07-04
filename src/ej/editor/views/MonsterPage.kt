package ej.editor.views

import ej.editor.AModView
import ej.editor.MonsterScope
import ej.editor.Styles
import ej.editor.utils.colspan
import ej.editor.utils.smartRow
import javafx.geometry.Pos
import javafx.util.converter.IntegerStringConverter
import tornadofx.*

abstract class AMonsterView(title:String?=null) : AModView(title) {
	override val scope = super.scope as MonsterScope
	val monster get() = scope.monster
}

class MonsterPage(): AMonsterView() {
	
	override val root = tabpane {
		tab<MonsterBasicView>()
		tab<MonsterBodyView>()
		tab<MonsterDescView>()
		tab<MonsterSpecialView>()
		tab<MonsterScriptsView>()
		tab<MonsterScenesView>()
	}
}

class MonsterBasicView:AMonsterView("Basic") {
	override val root = vbox {
		hbox(10) {
			form {
				spacing = 10.0
				fieldset("Core") {
					field("ID") { textfield(monster.idProperty) }
					field("Prorotype monster") {
						textfield(monster.baseId) // TODO select from other monsters
					}
					field("Name") { textfield(monster.name) }
				}
				fieldset("Grammar") {
					field("Group") {
						checkbox("(use plural)", monster.pluralProperty)
					}
					field("Article") { textfield(monster.article) }
					label("Pronouns:")
					field("'he'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(monster.pronouns.he)
					}
					field("'his'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(monster.pronouns.his)
					}
					field("'him'") {
						labelContainer.alignment = Pos.CENTER_RIGHT
						textfield(monster.pronouns.him)
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
							textfield(monster.combat.levelProperty, IntegerStringConverter()){ prefColumnCount = 3 }
						}
					}
					row {
						vbox {
							label("STR") { useMaxWidth = true }
							textfield(monster.combat.strProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("TOU") { useMaxWidth = true }
							textfield(monster.combat.touProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("SPE") { useMaxWidth = true }
							textfield(monster.combat.speProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("INT") { useMaxWidth = true }
							textfield(monster.combat.intProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("WIS") { useMaxWidth = true }
							textfield(monster.combat.wisProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							label("LIB") { useMaxWidth = true }
							textfield(monster.combat.libProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
					}
					smartRow {
						vbox {
							colspan(2)
							label("Sensitivity") { useMaxWidth = true }
							textfield(monster.combat.senProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							colspan(2)
							label("Corruption") { useMaxWidth = true }
							textfield(monster.combat.corProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
						vbox {
							colspan(2)
							label("Bonus HP") { useMaxWidth = true }
							textfield(monster.combat.bonusHpProperty, IntegerStringConverter()) { prefColumnCount = 3 }
						}
					}
				}
			}
		}
		
	}
}

class MonsterBodyView():AMonsterView("Body") {
	override val root = form {
		label("Body")
		// TODO
	}
}

class MonsterDescView():AMonsterView("Description") {
	val editor = XStatementTreeWithEditor()
	
	override val root = vbox {
		add(editor)
		editor.contents = monster.desc.content
	}
}

class MonsterSpecialView():AMonsterView("Specials") {
	override val root = form {
		label("Specials")
		// TODO
	}
}

class MonsterScriptsView():AMonsterView("Scripts") {
	override val root = form {
		label("Scripts")
		// TODO
	}
}

class MonsterScenesView():AMonsterView("Scenes") {
	override val root = form {
		label("Scenes")
		// TODO
	}
}