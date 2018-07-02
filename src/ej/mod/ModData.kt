package ej.mod

import ej.utils.ValidateElements
import ej.utils.ValidateNonBlank
import ej.utils.classValidatorFor
import java.io.InputStream
import java.io.Reader
import javax.xml.bind.JAXBContext
import javax.xml.bind.Unmarshaller
import javax.xml.bind.annotation.*

/*
 * Created by aimozg on 25.06.2018.
 * Confidential until published on GitHub
 */

interface ModDataNode

@XmlRootElement(name="mod")
class ModData : ModDataNode {
	@ValidateNonBlank
	@get:XmlAttribute
	var name:String = ""
	
	@get:XmlAttribute
	var version:Int = 0
	
	@ValidateElements(locator="name")
	@XmlElementWrapper(name="state")
	@XmlElement(name="var")
	val stateVars:MutableList<StateVar> = ArrayList()
	
	@XmlElement(name="hook")
	@ValidateElements()
	val hooks:MutableList<ModHookData> = ArrayList()
	
	@XmlElement(name="script")
	@ValidateElements()
	val scripts:MutableList<ModScript> = ArrayList()
	
	@XmlElement(name="monster")
	@ValidateElements(locator="id")
	val monsters:ArrayList<MonsterData> = ArrayList()
	
	@XmlElements(
			XmlElement(name="lib",type=XcLib::class),
			XmlElement(name="scene",type=XcScene::class),
			XmlElement(name="text",type=XcNamedText::class)
	)
	val content:ArrayList<StoryStmt> = ArrayList()
	
	override fun toString(): String {
		return "<mod name='$name' version='$version'>" +
				" <state> ${stateVars.joinToString(" ")} </state>"+
				hooks.joinToString(" ")+
				monsters.joinToString(" ")+
				content.joinToString(" ")+
				"</mod>"
	}
	
	fun validate() = VALIDATOR.validate(this)
	
	@Suppress("unused", "UNUSED_PARAMETER")
	private fun afterUnmarshal(unmarshaller: Unmarshaller, parent:Any){
		visit(StylingVisitor())
	}
	
	companion object {
		internal val VALIDATOR by lazy { classValidatorFor<ModData>() }
		val jaxbContext: JAXBContext by lazy {
			JAXBContext.newInstance(ModData::class.java)
		}
		fun loadMod(src: InputStream):ModData {
			return unmarshaller().unmarshal(src) as ModData
		}
		fun loadMod(src: Reader):ModData {
			return unmarshaller().unmarshal(src) as ModData
		}
		
		fun unmarshaller() = jaxbContext.createUnmarshaller()
	}
	
}

val DefaultModData by lazy {
	ModData.loadMod(ModData::class.java.getResourceAsStream("default.xml"))
}
