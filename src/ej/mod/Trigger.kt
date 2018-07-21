package ej.mod

import ej.editor.expr.ExpressionProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/*
 * Created by aimozg on 19.07.2018.
 * Confidential until published on GitHub
 */

sealed class SceneTrigger : ModDataNode{

}

class TimedTrigger : SceneTrigger() {
	val conditionProperty = ExpressionProperty("true")
	var condition by conditionProperty
	// TODO type
}

class EncounterTrigger : SceneTrigger() {
	val poolProperty = SimpleStringProperty("")
	var pool: String by poolProperty
	
	val conditionProperty = ExpressionProperty("true")
	var condition by conditionProperty
	
	val chanceProperty = ExpressionProperty("1")
	var chance by chanceProperty
}