package ej.editor.views

import ej.editor.AModView
import ej.mod.EncounterTrigger
import ej.mod.Natives
import tornadofx.*

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

class EncounterPage : AModView("Encounter"){
	lateinit var encounter:EncounterTrigger
	override val root = vbox {
		form {
			fieldset {
				field("Pool") {
					combobox(encounter.poolProperty,
					         Natives.encounterPools.map { it.id })
				}
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
	}
}