package ej.editor.views

import ej.editor.AModView
import tornadofx.*

class ModOverviewPage: AModView("Overview") {
	override val root = vbox {
		form {
			fieldset("Mod overview") {
				field("ID") {
					textfield(controller.mod?.nameProperty?:return@field)
				}
			}
		}
	}
}

