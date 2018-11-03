package ej.editor.external

import ej.mod.ModData
import ej.xml.*
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class NativePool : XmlAutoSerializable {
	@Attribute var id:String = ""
	@Attribute var desc:String? = null
}

class NativePlace : XmlAutoSerializable {
	@Attribute
	var id: String = ""
	@Attribute
	var desc: String? = null
}

class NativeMonster : XmlAutoSerializable {
	@Attribute var id:String = ""
	@Element var desc:String? = null
}

class NativeScene : XmlAutoSerializable {
	@Attribute var ref:String = ""
	@Element var desc:String? = null
}

class NativeItem : XmlAutoSerializable {
	@Attribute var id:String = ""
	@Element var desc:String? = null
}

class NativePerk : XmlAutoSerializable {
	@Attribute var id:String = ""
	@Attribute var name:String = ""
	@Element var desc:String? = null
}

@RootElement("gamedata")
class NativesClass : XmlAutoSerializable {
	@Elements("pool", true, "encounter-pools")
	val encounterPools = ArrayList<NativePool>()
	@Elements("monster", true)
	val monsters = ArrayList<NativeMonster>()
	@Elements("scene", true)
	val scenes = ArrayList<NativeScene>()
	@Elements("item",true)
	val items = ArrayList<NativeItem>()
	@Elements("perk",true)
	val perks = ArrayList<NativePerk>()
	@Elements("places", true)
	val places = ArrayList<NativePlace>()
}

val Natives: NativesClass by lazy {
	try {
		val f = File("natives.xml")
		if (f.exists() && f.canRead()) return@lazy loadNatives(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	loadNatives(ModData::class.java.getResourceAsStream("natives.xml"))
}

fun loadNatives(input: InputStream): NativesClass {
	return getSerializationInfo<NativesClass>().deserializeDocument(XmlExplorer(input))
}