package ej.mod

import ej.xml.Attribute
import ej.xml.TextBody
import ej.xml.XmlAutoSerializable

class StateVar : XmlAutoSerializable {
	@Attribute
	var name:String = ""
	
	@TextBody
	var initialValue:String = ""
	
	override fun toString(): String {
		return "<var name='$name'>$initialValue</var>"
	}
}