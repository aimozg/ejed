package ej.editor.expr.external

import ej.editor.expr.WithReadableText
import ej.editor.expr.parseExpressionSafe
import tornadofx.*
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlTransient

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
class EnumDecl {
	@get:XmlAttribute
	var name:String = ""

	@get:XmlElement(name="value")
	val values = ArrayList<EnumConstDecl>().observable()

	class EnumConstDecl : WithReadableText{
		override fun text(): String = listname

		@get:XmlAttribute
		var name:String = ""

		@get:XmlAttribute
		var impl:String = ""
		@get:XmlTransient
		val implExpr get() = parseExpressionSafe(impl)

		@get:XmlAttribute(name="listname")
		var listnameSpecial: String? = null
		@get:XmlTransient
		val listname get() = listnameSpecial ?: name

		@get:XmlElement(name="description")
		var description: String? = null
	}
}