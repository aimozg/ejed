package ej.editor.expr.external

import ej.editor.expr.WithReadableText
import ej.editor.expr.parseExpressionSafe
import ej.xml.Attribute
import ej.xml.Element
import ej.xml.Elements
import ej.xml.XmlAutoSerializable
import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
class EnumDecl : XmlAutoSerializable {
	@Attribute
	var name:String = ""

	@Elements("value")
	val values = ArrayList<EnumConstDecl>().observable()

	class EnumConstDecl : WithReadableText, XmlAutoSerializable {
		override fun text(): String = name

		@Attribute
		var name:String = ""

		@Attribute
		var impl:String = ""
		val implExpr get() = parseExpressionSafe(impl)

		@Element
		var description: String? = null
	}
}
