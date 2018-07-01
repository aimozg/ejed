package ej.mod

import com.sun.xml.internal.txw2.annotation.XmlElement
import ej.editor.utils.escapeXml
import ej.utils.affix
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlValue

class XsTextNode() : XStatement {
	constructor(content:String):this() {
		this.content = content
	}
	
	var content:String = ""
	
	override val tagName get() = ""
	override fun innerXML() = content.escapeXml()
	override fun toSourceString() = innerXML()
	override fun toString() = content
}


@XmlRootElement(name = "display")
class XsDisplay : XStatement {
	@get:XmlAttribute
	var ref: String = ""
	
	override val tagName get() = "display"
	override val emptyTag get() = true
	
	override fun attrsString() = "ref='$ref'"
	
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
	
	override val tagName get() = "set"
	override val emptyTag get() = true
	override fun attrsString(): String = "var='$varname'" +
			inobj.affix(" in='", "'") +
			op.affix(" op='", "'") +
			" value='$value'/>"
}

@XmlRootElement(name = "output")
class XsOutput : XStatement {
	@get:XmlValue
	var expression: String = ""
	
	override val tagName get() = "output"
	override fun innerXML(): String = expression
}

@XmlRootElement(name = "menu")
class XsMenu : XStatement {
	@get:XmlElement("button")
	val buttons: MutableList<XsButton> = ArrayList()
	
	override val tagName get() = "menu"
	override fun innerXML(): String = buttons.joinToSourceString()
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
	
	override fun innerXML() = hint?.toSourceString()?:""
	
	override val emptyTag: Boolean = hint == null
	override val tagName get() = "button"
	override fun attrsString(): String = "" +
			"text='$text'" +
			(if (disabled) " disabled" else "") +
			" call='$call'>"
}

class XsButtonHint : XContentContainer("hint") {
	@get:XmlAttribute
	var header: String? = null
	
	override fun attrsString() = header.affix("header='", "'")
}

@XmlRootElement(name = "next")
class XsNext : XStatement {
	@get:XmlValue
	var call: String = ""
	
	override val tagName get() = "next"
	override fun innerXML() = call
}

@XmlRootElement(name = "battle")
class XsBattle : XStatement {
	@get:XmlAttribute
	var monster: String = ""
	
	@get:XmlAttribute
	var options: String? = ""
	
	override val tagName get() = "battle"
	override val emptyTag get() = true
	override fun attrsString() = "monster='$monster'" +
			options.affix(" options='", "'")
}