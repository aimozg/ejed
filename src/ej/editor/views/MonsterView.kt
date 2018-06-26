package ej.editor.views

import ej.editor.AModFragment
import ej.editor.AModView
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
	override val root = form {
		val vm = controller.monsterVM
		fieldset("Core") {
			field("ID") { textfield(vm.id) }
			field("Base monster") {
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
			field("'he'") { textfield(vm.pronounHe) }
			field("'his'") { textfield(vm.pronounHis) }
			field("'him'") { textfield(vm.pronounHim) }
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