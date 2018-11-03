package ej.editor.external

import ej.mod.ModData
import ej.xml.*
import java.io.File
import java.io.InputStream

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

class TagDecl : XmlAutoSerializable {
	enum class Context {
		PLAYER,
		GAME,
		NPC
	}
	
	@Attribute
	var name: String = ""
	
	@Attribute
	var sample: String = ""
	
	@Attribute
	var context: Context = Context.PLAYER
	
	@Element
	var description: String? = null
	
	@PolymorphicElements(polymorphisms = [
		Polymorphism("pick-any", Part.PickAny::class),
		Polymorphism("pick-first", Part.PickFirst::class),
		Polymorphism("group", Part.Group::class),
		Polymorphism("text", Part.Text::class)
	])
	val parts = ArrayList<Part>()
	
	sealed class Part : XmlAutoSerializable {
		@Attribute("if")
		var condition: String? = null
		
		@Attribute
		var chance: Double = 1.0
		
		@Attribute
		var weight: Double = 1.0
		
		abstract class Container : Part() {
			@PolymorphicElements(polymorphisms = [
				Polymorphism("pick-any", Part.PickAny::class),
				Polymorphism("pick-first", Part.PickFirst::class),
				Polymorphism("group", Part.Group::class),
				Polymorphism("text", Part.Text::class)
			])
			val parts = ArrayList<Part>()
		}
		
		class PickAny : Container()
		class PickFirst : Container()
		class Group : Container()
		class Text : Part() {
			@TextBody
			var content: String = ""
			@Attribute
			var parse: Boolean = false
		}
	}
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
	
	val tags: MutableMap<String, TagDecl> = HashMap()
	
	@AfterLoad
	private fun buildTags() {
		tags.clear()
		tags.putAll(taglist.map { it.name to it })
		tags.putAll(aliasList.map {
			tags[it.tag]?.let { tag -> it.name to tag } ?: error("Alias to unknown tag ${it.tag}")
		})
	}
}

val TagLib: TagLibClass by lazy {
	try {
		val f = File("tags.xml")
		if (f.exists() && f.canRead()) return@lazy loadTaglib(f.inputStream())
	} catch (e:Exception) {
		e.printStackTrace()
	}
	loadTaglib(ModData::class.java.getResourceAsStream("tags.xml"))
}

fun loadTaglib(input: InputStream): TagLibClass {
	return getSerializationInfo<TagLibClass>().deserializeDocument(XmlExplorer(input))
}