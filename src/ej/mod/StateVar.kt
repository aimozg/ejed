package ej.mod

import ej.utils.ValidateNonBlank
import ej.utils.ValidateUnique
import ej.utils.classValidatorFor
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlValue

class StateVar {
	@ValidateNonBlank
	@ValidateUnique
	@get:XmlAttribute
	var name:String = ""
	
	@get:XmlValue
	var initialValue:String = ""
	
	fun validate(context: ModData) = VALIDATOR.validate(this, context.stateVars)
	override fun toString(): String {
		return "<var name='$name'>$initialValue</var>"
	}
	
	
	companion object {
		internal val VALIDATOR by lazy { classValidatorFor<StateVar>() }
	}
}