package ej.editor.expr.external

import org.funktionale.either.Either
import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class FunctionDecl {
	var name:String = ""

	var returnTypeRaw=""

	var listnameSpecial:String? = null
	val listname:String = listnameSpecial ?: name

	var description:String? = null

	val params = ArrayList<ParamDecl>().observable()
	fun paramByName(name:String) = params.firstOrNull { it.name == name }

	// TODO editor
	// TODO impl

	class ParamDecl {
		// TODO
		var name:String = ""
		var type:String = ""

	}

	var editor = EditorDecl()

	class EditorDecl {
		val partsRaw = ArrayList<Any>()

		val parts: List<Either<ParamDecl, String>>
			get() = partsRaw.map {
			if (it is ParamRef) Either.left(function.paramByName(it.name)!!)
			else Either.right(it.toString())
		}

		lateinit var function:FunctionDecl

		class ParamRef {
			var name:String = ""
		}
	}
}