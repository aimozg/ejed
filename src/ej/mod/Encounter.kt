package ej.mod

import ej.utils.ValidateNonBlank
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

/*
encounter = element encounter {
    attribute pool { text }
    & attribute name { text }
    & element condition {
        expression
    }?
    & element chance {
        expression
    }?
    & element scene {
        content*,
        scene-fin
    }
}
 */

@XmlRootElement(name="encounter")
class Encounter : ModDataNode {
	@XmlTransient
	val poolProperty = SimpleStringProperty("")
	@ValidateNonBlank
	@get:XmlAttribute
	var pool:String by poolProperty
	
	@XmlTransient
	val nameProperty = SimpleStringProperty("")
	@ValidateNonBlank
	@get:XmlAttribute
	var name by nameProperty
	
	@XmlTransient
	val conditionProperty = SimpleStringProperty()
	@get:XmlElement
	var condition by conditionProperty
	
	@XmlTransient
	val chanceProperty = SimpleStringProperty()
	@get:XmlElement
	var chance by chanceProperty
	
	@XmlTransient
	val sceneProperty = SimpleObjectProperty(EncounterScene())
	@get:XmlElement
	var scene by sceneProperty
	
	@XmlRootElement(name="scene")
	class EncounterScene : XContentContainer("scene")
	
}