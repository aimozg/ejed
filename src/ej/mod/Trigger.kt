package ej.mod

import ej.editor.expr.ExpressionProperty
import ej.xml.Attribute
import ej.xml.XmlAutoSerializable
import ej.xml.XmlSerializable
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/*
 * Created by aimozg on 19.07.2018.
 * Confidential until published on GitHub
 */

sealed class SceneTrigger : ModDataNode, XmlSerializable

class TimedTrigger : SceneTrigger(), XmlAutoSerializable {
	val conditionProperty = ExpressionProperty("true")
	@Attribute
	var condition:String by conditionProperty
	
	val typeProperty = SimpleObjectProperty(Type.DAILY)
	@Attribute
	var type: Type by typeProperty
	
	enum class Type {
		DAILY,
		HOURLY
	}
}

class EncounterTrigger : SceneTrigger(), XmlAutoSerializable {
	val poolProperty = SimpleStringProperty("")
	@Attribute
	var pool: String by poolProperty
	
	val conditionProperty = ExpressionProperty("true")
	@Attribute
	var condition: String by conditionProperty
	
	val chanceProperty = ExpressionProperty("1")
	@Attribute
	var chance: String by chanceProperty
}