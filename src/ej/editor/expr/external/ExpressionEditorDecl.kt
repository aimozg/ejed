package ej.editor.expr.external

import ej.utils.Validable
import ej.utils.ValidationReport
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

class ExpressionEditorDecl : XmlAutoSerializable,Validable {
	@ParentElement
	lateinit var expression: ExpressionDecl
	
	@MixedToEitherBody(Polymorphism("param", ParamRef::class))
	val parts = ArrayList<Either<String,ParamRef>>().observable()

	override fun validate() = ValidationReport.build {
		validateAll(parts.asSequence().filterIsInstance<ParamRef>())
	}
	
	inner class ParamRef : XmlAutoSerializable, Validable {
		@Attribute
		var name:String = ""
		@Attribute
		var type:ParamEditorType = ParamEditorType.LINK
		
		val decl:ParamDecl? get() = expression.paramByName(name)
		
		override fun validate() = ValidationReport.build {
			assertNotNull("param $name",decl)
		}
	}
	
	enum class ParamEditorType {
		LINK,
		SELECT,
		INPUT,
		CHECKBOX
	}
}