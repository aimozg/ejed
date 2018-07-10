package ej.mod

import com.sun.xml.internal.txw2.annotation.XmlElement
import ej.utils.affix
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient
import javax.xml.bind.annotation.XmlValue


@XmlRootElement(name = "display")
class XsDisplay : XStatement {
	@XmlTransient
	val refProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var ref: String by refProperty
	
	override fun toString() = defaultToString("display","",ref)
}

@XmlRootElement(name = "set")
class XsSet : XStatement {
	@XmlTransient
	val varnameProperty = SimpleObjectProperty("")
	@get:XmlAttribute(name = "var")
	var varname: String by varnameProperty
	
	@XmlTransient
	val inobjProperty = SimpleObjectProperty<String>( "")
	@get:XmlAttribute(name = "in")
	var inobj: String by inobjProperty
	
	// TODO enum
	@XmlTransient
	val opProperty = SimpleObjectProperty<String?>( null)
	@get:XmlAttribute(name = "op")
	var op: String? by opProperty
	
	@XmlTransient
	val valueProperty = SimpleObjectProperty("")
	@get:XmlAttribute(name = "value")
	var value: String by valueProperty
	
	
	override fun toString() = defaultToString(
			"set",
			"var='$varname'" +
			inobj.affix(" in='", "'") +
			op.affix(" op='", "'") +
			" value='$value'",
			"")
}

@XmlRootElement(name = "output")
class XsOutput : XStatement {
	@XmlTransient
	val expressionProperty = SimpleStringProperty("")
	@get:XmlValue
	var expression by expressionProperty
	
	override fun toString() = defaultToString("output","",expression)
}

@XmlRootElement(name = "menu")
class XsMenu : XContentContainer() {
	
	override fun toString() = defaultToString("menu","")
}

@XmlRootElement(name = "button")
class XsButton() : XStatement {
	constructor(text:String):this() {
		this.text =text
	}
	
	@XmlTransient
	val textProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var text by textProperty
	
	@XmlTransient
	val disabledProperty = SimpleBooleanProperty(false)
	@get:XmlAttribute
	var disabled by disabledProperty
	
	@XmlTransient
	val refProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var ref by refProperty
	
	@XmlTransient
	val posProperty = SimpleObjectProperty<Int?>(null)
	@get:XmlAttribute
	var pos:Int? by posProperty
	
	@XmlTransient
	val hintProperty = SimpleObjectProperty<XsButtonHint>()
	@get:XmlElement
	var hint by hintProperty
	
	
	override fun toString() = defaultToString("button", "" +
			"text='$text'" +
			(if (disabled) " disabled" else "") +
			" ref='$ref'", hint?.toString()?:"")
}

class XsButtonHint : XContentContainer() {
	@get:XmlAttribute
	var header: String? = null
	
	override fun toString() = defaultToString("hint", header.affix("header='", "'"))
}

@XmlRootElement(name = "next")
class XsNext : XStatement {
	@XmlTransient
	val refProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var ref by refProperty
	
	
	override fun toString() = defaultToString("next", "", ref)
}

@XmlRootElement(name = "battle")
class XsBattle : XStatement {
	@XmlTransient
	val monsterProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var monster by monsterProperty
	
	@XmlTransient
	val optionsProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var options by optionsProperty
	
	override fun toString() = defaultToString("battle",options.affix(" options='", "'"),monster)
}