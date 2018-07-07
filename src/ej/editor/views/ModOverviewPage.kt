package ej.editor.views

import ej.editor.AModView
import ej.mod.StateVar
import javafx.scene.control.TableView
import javafx.scene.layout.Priority
import tornadofx.*

class ModOverviewPage: AModView("Overview") {
	val varlist = TableView<StateVar>()
	override val root = vbox {
		form {
			fieldset("Mod overview") {
				field("ID") {
					textfield(mod.nameProperty)
				}
				field("Version") {
					textfield(mod.versionProperty)
				}
			}
			hbox {
				hgrow = Priority.ALWAYS
				label("Variables"){
					addClass(Stylesheet.legend)
					hgrow = Priority.ALWAYS
				}
				button("+") {
					action {
						mod.stateVars.add(
								StateVar().apply {
									name = "flag${mod.stateVars.size+1}"
								})
					}
				}
				button("-") {
					disableWhen(varlist.selectionModel.selectedItemProperty().isNull)
					action {
						mod.stateVars.remove(varlist.selectedItem?:return@action)
					}
				}
			}
			varlist.attachTo(this) {
				items = mod.stateVars
				column("Name",StateVar::name).makeEditable()
				column("Initial value",StateVar::initialValue).makeEditable()
			}
		}
	}
}

