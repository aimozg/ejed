package ej.editor.expr.external

import ej.editor.expr.WithReadableText
import ej.editor.expr.parseExpressionSafe
import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */
class EnumDecl {
	var name:String = ""

	val values = ArrayList<EnumConstDecl>().observable()

	class EnumConstDecl : WithReadableText{
		override fun text(): String = listname

		var name:String = ""

		var impl:String = ""
		val implExpr get() = parseExpressionSafe(impl)

		var listnameSpecial: String? = null
		val listname get() = listnameSpecial ?: name

		var description: String? = null
	}
}