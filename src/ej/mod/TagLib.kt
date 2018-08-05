package ej.mod

import ej.xml.*
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class TagDecl : XmlAutoSerializable {
	@Attribute var name:String = ""
	@Attribute var sample:String = ""
}

class TagAlias : XmlAutoSerializable {
	@Attribute var name:String = ""
	@Attribute var tag:String = ""
}

@RootElement("tags")
class TagLibClass : XmlAutoSerializable {
	@Elements("tag")
	private val taglist = ArrayList<TagDecl>()
	@Elements("alias")
	private val aliasList = ArrayList<TagAlias>()
	
	val tags: MutableMap<String,TagDecl> = HashMap()
	
	@AfterLoad
	private fun buildTags() {
		tags.clear()
		tags.putAll(taglist.map { it.name to it })
		tags.putAll(aliasList.map {
			tags[it.tag]?.let { tag -> it.name to tag } ?: error("Alias to unknown tag ${it.tag}")
		})
	}
}

val TagLib:TagLibClass by lazy {
	try {
		val f = File("tags.xml")
		if (f.exists() && f.canRead()) return@lazy loadTaglib(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	loadTaglib(NativesClass::class.java.getResourceAsStream("tags.xml"))
}

fun loadTaglib(input: InputStream):TagLibClass {
	return getSerializationInfo<TagLibClass>().deserializeDocument(XmlExplorer(input))
}