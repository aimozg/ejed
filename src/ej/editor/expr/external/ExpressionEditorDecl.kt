package ej.editor.expr.external

import ej.editor.utils.transformed
import ej.xml.*
import org.funktionale.either.Either
import tornadofx.*

class ParamDecl : XmlAutoSerializable {
	// TODO
	@Attribute
	var name:String = ""
	@Attribute
	var type:String = ""
	@Attribute
	var default:String? = null
}

class ExpressionEditorDecl : XmlAutoSerializable {
	
	@MixedBody(polymorphisms =[Polymorphism("param",ParamRef::class)])
	val partsRaw = ArrayList<Any>().observable()

	val parts: List<Either<ParamDecl, String>> = partsRaw.transformed {
		if (it is ParamRef) Either.left(expression.paramByName(it.name) ?: kotlin.error("Unknown parameter ${it.name}"))
		else Either.right(it.toString())
	}

	@ParentElement
	lateinit var expression: ExpressionDecl

	class ParamRef : XmlAutoSerializable {
		@Attribute
		var name:String = ""
	}
	
}