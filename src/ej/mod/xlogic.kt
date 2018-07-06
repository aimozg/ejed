package ej.mod

import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name="if")
class XlIf : XContentContainer(), XStatement {
	
	@get:XmlAttribute
	var test: String by property("")
	fun testProperty() = getProperty(XlIf::test)
	
	override fun toString() = defaultToString("if","test=$test")
}

@XmlRootElement(name="else")
class XlElse : XStatement {
	override fun toString() = "[else]"
}

@XmlRootElement(name="elseif")
class XlElseIf : XStatement {
	@get:XmlAttribute
	var test:String = ""
	
	override fun toString() = defaultToString("elseif","test='$test'","")
}

@XmlRootElement(name="switch")
class XlSwitch : XStatement {
	init {
		TODO("<switch> NYI")
	}
	override fun toString() = defaultToString("switch","","")
	
}