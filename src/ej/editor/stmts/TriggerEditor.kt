package ej.editor.stmts

import ej.editor.expr.lists.AnyExprChooser
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.external.Natives
import ej.editor.utils.nodeBinding
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 04.10.2018.
 * Confidential until published on GitHub
 */

class TimedTriggerEditor(val trigger: TimedTrigger) : VBox() {
	init {
		field("Type") {
			combobox(trigger.typeProperty, TimedTrigger.Type.values().asList()) {
				cellFormat(DefaultScope) { type -> type?.name?.toLowerCase() }
			}
		}
		field("Condition") {
			textflow {
				valueLink(trigger.conditionProperty.builderProperty,
				          "Condition",
				          BoolExprChooser)
			}
		}
	}
}

class EncounterTriggerEditor(val trigger: EncounterTrigger) : VBox() {
	init {
		field("Pool") {
			combobox(trigger.poolProperty,
			         Natives.encounterPools.map { it.id }) {
				cellFormat(DefaultScope) { poolId ->
					text = Natives.encounterPools.find { it.id == poolId }?.desc ?: poolId
				}
			}
		}
		field("Chance") {
			textflow {
				valueLink(trigger.chanceProperty.builderProperty,
				          "Chance",
				          AnyExprChooser)
			}
		}
		field("Condition") {
			textflow {
				valueLink(trigger.conditionProperty.builderProperty,
				          "Condition",
				          BoolExprChooser)
			}
		}
	}
}

class PlaceTriggerEditor(val trigger: PlaceTrigger) : VBox() {
	init {
		field("Location") {
			combobox(trigger.placeProperty,
			         Natives.places.map { it.id }) {
				cellFormat(DefaultScope) { placeId ->
					text = Natives.places.find { it.id == placeId }?.desc ?: placeId
				}
			}
		}
		field("Name") {
			textfield(trigger.nameProperty)
		}
		field("Condition") {
			textflow {
				valueLink(trigger.conditionProperty.builderProperty,
				          "Condition",
				          BoolExprChooser)
			}
		}
	}
}

class SceneTriggerEditor(val scene: XcScene) : Form() {
	
	enum class TriggerType(
			val displayName: String,
			val klass: Class<out SceneTrigger>?,
			val newEditor: (SceneTrigger?) -> Node?
	) {
		NONE("none", null,
		     { null }),
		TIMED("timed", TimedTrigger::class.java,
		      { TimedTriggerEditor(it as TimedTrigger) }),
		ENCOUNTER("encounter", EncounterTrigger::class.java,
		          { EncounterTriggerEditor(it as EncounterTrigger) }),
		PLACE("place", PlaceTrigger::class.java,
		      { PlaceTriggerEditor(it as PlaceTrigger) });
		
		fun accepts(trigger: SceneTrigger?) = trigger?.javaClass == klass
		fun newTrigger() = klass?.newInstance()
	}
	
	fun getTriggerType(trigger: SceneTrigger?) = TriggerType.values().first {
		it.accepts(trigger)
	}
	
	fun XcScene.setTriggerType(tt: TriggerType) {
		if (getTriggerType(trigger) == tt) return
		trigger = tt.newTrigger()
	}
	
	val triggerTypeProperty = SimpleObjectProperty<TriggerType>(getTriggerType(scene.trigger))
	
	init {
		addClass("trigger-editor")
		triggerTypeProperty.onChange {
			if (it != null) scene.setTriggerType(it)
		}
		fieldset {
			field("Trigger type") {
				combobox(triggerTypeProperty, TriggerType.values().asList()) {
					cellFormat(DefaultScope) {
						text = item.displayName
					}
				}
			}
			nodeBinding(this@SceneTriggerEditor.scene.triggerProperty) { t ->
				getTriggerType(t).newEditor(t)
			}
		}
	}
}


