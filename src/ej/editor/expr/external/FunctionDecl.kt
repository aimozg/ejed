package ej.editor.expr.external

import org.funktionale.either.Either
import tornadofx.*
import javax.xml.bind.annotation.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class FunctionDecl {
	@get:XmlAttribute
	var name:String = ""

	@get:XmlAttribute(name="return")
	var returnTypeRaw=""

	@get:XmlElement(name="listname")
	var listnameSpecial:String? = null
	@get:XmlTransient
	val listname:String = listnameSpecial ?: name

	@get:XmlElement(name="description")
	var description:String? = null

	@get:XmlElement(name="param")
	val params = ArrayList<ParamDecl>().observable()
	fun paramByName(name:String) = params.firstOrNull { it.name == name }

	// TODO editor
	// TODO impl

	class ParamDecl {
		// TODO
		@get:XmlAttribute
		var name:String = ""
		@get:XmlAttribute
		var type:String = ""

	}

	@get:XmlElement(name="editor")
	var editor = EditorDecl()

	class EditorDecl {
		@XmlMixed
		@XmlElementRef(name="param",type=ParamRef::class)
		val partsRaw = ArrayList<Any>()

		@get:XmlTransient
		val parts: List<Either<ParamDecl, String>>
			get() = partsRaw.map {
			if (it is ParamRef) Either.left(function.paramByName(it.name)!!)
			else Either.right(it.toString())
		}

		@get:XmlTransient
		lateinit var function:FunctionDecl

		class ParamRef {
			@get:XmlAttribute
			var name:String = ""
		}
	}
}