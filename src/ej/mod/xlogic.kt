package ej.mod

import javafx.beans.property.SimpleStringProperty
import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlTransient

@XmlRootElement(name="comment")
class XlComment(): XStatement {
	constructor(text:String):this(){
		this.text = text
	}
	
	@XmlTransient
	val textProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var text: String by textProperty
	
	override fun toString() = "[# $text #]"
}

@XmlRootElement(name="if")
class XlIf() : XContentContainer(), XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	@XmlTransient
	val testProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var test: String by testProperty
	
	override fun toString() = defaultToString("if","test=$test")
}

@XmlRootElement(name="else")
class XlElse : XStatement {
	override fun toString() = "[else]"
}

@XmlRootElement(name="elseif")
class XlElseIf() : XStatement {
	constructor(test:String):this() {
		this.test = test
	}
	
	@XmlTransient
	val testProperty = SimpleStringProperty("")
	@get:XmlAttribute
	var test: String by testProperty
	
	override fun toString() = defaultToString("elseif","test='$test'","")
}

@XmlRootElement(name="switch")
class XlSwitch : XStatement {
	init {
		TODO("<switch> NYI")
	}
	override fun toString() = defaultToString("switch","","")
	
}