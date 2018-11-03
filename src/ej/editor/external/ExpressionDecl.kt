package ej.editor.external

import ej.utils.Validable
import ej.utils.ValidationReport
import ej.xml.*
import tornadofx.*

/*
 * Created by aimozg on 23.07.2018.
 * Confidential until published on GitHub
 */

abstract class ExpressionDecl : XmlAutoSerializable, Validable {
	@Attribute
	var name: String = ""
	
	@Element
	var listname: String? = null
	
	@Element
	var description: String? = null
	
	@Elements("param")
	val params = ArrayList<ParamDecl>().observable()
	
	@Element("editor")
	var editor = ExpressionEditorDecl()
	
	@Element("editor-hint")
	@TextBodyWhitespacePolicy(WhitespacePolicy.TRIM)
	var editorHint: String? = null
	
	val arity get() = params.size
	
	fun paramByName(name: String) = params.firstOrNull { it.name == name }
	
	override fun validate() = ValidationReport.build {
		validate(editor,"editor")
	}
}