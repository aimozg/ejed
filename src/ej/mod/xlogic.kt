package ej.mod

import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name="if")
class XlIf : XContentContainer("if"), XStatement {
	
	@get:XmlAttribute
	var test: String by property("")
	fun testProperty() = getProperty(XlIf::test)
	
	override fun attrsString() = "test='$test'"
}

@XmlRootElement(name="else")
class XlElse : XStatement {
	override val tagName get() = "else"
	override val emptyTag get() = true
	
}

@XmlRootElement(name="elseif")
class XlElseIf : XStatement {
	@get:XmlAttribute
	var test:String = ""
	
	override fun attrsString() = "test='$test'"
	override val tagName get() = "elseif"
	override val emptyTag get() = true
}

@XmlRootElement(name="switch")
class XlSwitch : XStatement {
	init {
		TODO("<switch> NYI")
	}
	override val tagName get() = "switch"
	
}