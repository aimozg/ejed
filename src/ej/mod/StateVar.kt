package ej.mod

import ej.xml.HasSzInfo
import ej.xml.XmlSerializable
import ej.xml.XmlSzInfoBuilder
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlValue

class StateVar : XmlSerializable {
	@get:XmlAttribute
	var name:String = ""
	
	@get:XmlValue
	var initialValue:String = ""
	
	override fun toString(): String {
		return "<var name='$name'>$initialValue</var>"
	}
	
	
	companion object : HasSzInfo<StateVar> {
		override val szInfoClass = StateVar::class
		
		override fun XmlSzInfoBuilder<StateVar>.buildSzInfo() {
			attr(StateVar::name)
			text(StateVar::initialValue)
		}
	}
}