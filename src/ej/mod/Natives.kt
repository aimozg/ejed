package ej.mod

import ej.xml.XmlExplorer
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class NativePool(var id:String, var desc:String)

class NativeMonster(var id:String)

class NativeScene(var ref:String, var desc:String)

class NativesClass {
	val encounterPools = ArrayList<NativePool>()
	val monsters = ArrayList<NativeMonster>()
	val scenes = ArrayList<NativeScene>()
}

val Natives:NativesClass by lazy {
	try {
		val f = File("natives.xml")
		if (f.exists() && f.canRead()) return@lazy loadNatives(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	loadNatives(NativesClass::class.java.getResourceAsStream("natives.xml"))
}

fun loadNatives(input: InputStream):NativesClass {
	val n = NativesClass()
	
	XmlExplorer(input).exploreDocumentElements("gamedata") { tag, _ ->
		when (tag) {
			"encounter-pools" -> forEachElement("pool") { attrs ->
				n.encounterPools.add(NativePool(
						attrs["id"]!!, attrs["desc"]?:""
				))
			}
			"monsters" -> forEachElement("monster") { attrs ->
				n.monsters.add(NativeMonster(attrs["id"]!!))
			}
			"items" -> {} // TODO load items
			"scenes" -> forEachElement("scene") { attrs ->
				val s = NativeScene(attrs["ref"]!!,"")
				forEachElement { tag2, _ -> when(tag2) {
					"desc" -> s.desc = text()
					else -> error("Unknown tag $tag2")
				} }
				n.scenes.add(s)
			}
			else -> error("Unknown tag $tag")
		}
	}
	
	return n
}