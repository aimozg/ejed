package ej.editor.views

import ej.editor.AModView
import ej.mod.Builtins
import ej.mod.Encounter
import javafx.scene.layout.Priority
import tornadofx.*

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

class EncounterScope(val encounter: Encounter): Scope()

class EncounterPage : AModView("Encounter"){
	override val scope = super.scope as EncounterScope
	val encounter get() = scope.encounter
	override val root = vbox {
		form {
			fieldset {
				field("Pool") {
					combobox(encounter.poolProperty,
					         Builtins.encounterPools)
				}
				field("Name") { textfield(encounter.nameProperty) }
				field("Chance") {
					textfield(encounter.chanceProperty) {
						promptText = "(default '1' - as often as other encounters)"
					}
				}
				field("Condition") {
					textfield(encounter.conditionProperty){
						promptText = "(default 'true')"
					}
				}
			}
		}
		StatementTreeWithEditor(mod).attachTo(this) {
			rootStatement = encounter.scene
			vgrow = Priority.SOMETIMES
		}
	}
}