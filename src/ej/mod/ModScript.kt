package ej.mod

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue
import javax.xml.bind.annotation.XmlValue

class ModScript {
	@get:XmlAttribute
	var language: ScriptLanguage = ScriptLanguage.LUA
	
	@get:XmlValue
	var content:String = ""
	
	override fun toString() = "<script language='$language'> $content </script>"
}

@XmlEnum(String::class)
enum class ScriptLanguage {
	@XmlEnumValue("lua")
	LUA,
	@XmlEnumValue("xlogic")
	XLOGIC;
}