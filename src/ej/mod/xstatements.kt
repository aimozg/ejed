package ej.mod

import com.sun.xml.internal.txw2.annotation.XmlElement
import ej.utils.affix
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlValue


@XmlRootElement(name = "display")
class XsDisplay : XStatement {
	@get:XmlAttribute
	var ref: String = ""
	
	override fun toString() = defaultToString("display","",ref)
}

@XmlRootElement(name = "set")
class XsSet : XStatement {
	@get:XmlAttribute(name = "var")
	var varname: String = ""
	@get:XmlAttribute(name = "in")
	var inobj: String? = null
	@get:XmlAttribute(name = "op")
	var op: String? = null // TODO enum
	@get:XmlAttribute(name = "value")
	var value: String = ""
	
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
	@get:XmlValue
	var expression: String = ""
	
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
	@get:XmlAttribute
	var monster: String = ""
	
	@get:XmlAttribute
	var options: String? = ""
	
	override fun toString() = defaultToString("battle",options.affix(" options='", "'"),monster)
}