package ej.mod

import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlEnum
import javax.xml.bind.annotation.XmlEnumValue

class ModHookData : XContentContainer("hook") {
	@get:XmlAttribute
	var type: HookType = HookType.DAILY
	
	override fun attrsString() = "type='$type'"
}

@XmlEnum(String::class)
enum class HookType {
	@XmlEnumValue("daily")
	DAILY,
	@XmlEnumValue("hourly")
	HOURLY;
	
	
}