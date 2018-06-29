package ej.editor.views

import ej.editor.AModView
import ej.editor.MonsterViewModel
import tornadofx.*


class ModView: AModView("Mod view") {
	init {
		headingProperty.bind(modVM.name)
		controller.monsterProperty.onChange {
			if (it != null) {
				workspace.dock(MonsterView(MonsterViewModel(modVM,it)))
			}
		}
	}
	
	
	override val root = borderpane {
		top {
		}
		left {
			vbox {
				label("Monsters")
				listview(modVM.monsters) {
					cellFormat { text = it.id }
					onUserSelect {
						controller.selectMonster(it)
					}
//					bindSelected(controller.monsterProperty)
				}
			}
		}
		center {
			this += find<ModOverviewFragment>().root
		}
	}
}