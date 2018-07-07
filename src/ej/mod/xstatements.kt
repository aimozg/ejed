package ej.mod

import com.sun.xml.internal.txw2.annotation.XmlElement
import ej.utils.affix
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
	val inobjProperty = SimpleObjectProperty<String?>( null)
	@get:XmlAttribute(name = "in")
	var inobj: String? by inobjProperty
	
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
class XsMenu : XStatement {
	@get:XmlElement("button")
	val buttons: MutableList<XsButton> = ArrayList()
	
	override fun toString() = defaultToString("menu","",buttons.joinToString(" "))
}

@XmlRootElement(name = "button")
class XsButton : XStatement {
	@get:XmlAttribute
	var text: String = ""
	
	@get:XmlAttribute
	var disabled: Boolean = false
	
	@get:XmlAttribute
	var call: String = ""
	
	@get:XmlElement
	var hint: XsButtonHint? = null
	
	override fun toString() = defaultToString("button","" +
			"text='$text'" +
			(if (disabled) " disabled" else "") +
			" call='$call'",hint?.toString()?:"")
}

class XsButtonHint : XContentContainer() {
	@get:XmlAttribute
	var header: String? = null
	
	override fun toString() = defaultToString("hint", header.affix("header='", "'"))
}

@XmlRootElement(name = "next")
class XsNext : XStatement {
	@get:XmlValue
	var call: String = ""
	
	override fun toString() = defaultToString("next","",call)
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