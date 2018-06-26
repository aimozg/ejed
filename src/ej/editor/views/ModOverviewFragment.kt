package ej.editor.views

import ej.editor.AModFragment
import tornadofx.*

class ModOverviewFragment: AModFragment("Overview") {
	override val root = vbox {
		form {
			fieldset("Mod overview") {
				field("ID") {
					textfield(modVM.name)
				}
			}
			button("Save") {
				action {
					modVM.commit()
				}
			}
		}
	}
}

