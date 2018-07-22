package ej.mod

import ej.xml.XmlSerializable
import ej.xml.XmlSerializableCompanion
import ej.xml.XmlSzInfoBuilder

class ModScript : XmlSerializable {
	var language: ScriptLanguage = ScriptLanguage.LUA
	
	var content:String = ""
	
	override fun toString() = "<script language='$language'> $content </script>"
	
	companion object : XmlSerializableCompanion<ModScript> {
		override val szInfoClass = ModScript::class
		
		override fun XmlSzInfoBuilder<ModScript>.buildSzInfo() {
			attribute(ModScript::language, {it.name.toLowerCase()})
			textBody(ModScript::content)
		}
		
	}
}

enum class ScriptLanguage {
	LUA,
	XLOGIC;
}