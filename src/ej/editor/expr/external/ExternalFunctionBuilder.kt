package ej.editor.expr.external

import ej.editor.expr.*
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.expr.lists.BoolExprChooser
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class ExternalFunctionBuilder(
		val decl: FunctionDecl
) : ExpressionBuilder() {
	val params = decl.params.map { SimpleObjectProperty<ExpressionBuilder?>(this,it.name,null) }
	val paramsByName = params.associateBy { it.name }

	@Suppress("IMPLICIT_CAST_TO_ANY")
	override fun text(): String {
		return mktext(decl.editor.parts.map {(l,r)->
			if (l != null) paramsByName[l.name]
			else r
		})
	}

	// TODO if impl present
	override fun build() = CallExpression(
			Identifier(decl.name),
			params.map { it.value?.build()?: nop() }
	)

	override fun editorBody() = defaultEditorTextFlow {
		for ((l, r) in decl.editor.parts) {
			val prop = paramsByName[l?.name]
			if (l != null && prop != null) {
				val chooser: ExpressionChooser = when (l.type) {
					"boolean" -> BoolExprChooser
					/* TODO pick proper chooser*/
					else -> AnyExprChooser
				}
				valueLink(prop, l.name, chooser)
			}
			if(r != null) text(r)
		}
	}

	override fun name() = decl.listname

	override fun copyMe() = ExternalFunctionBuilder(decl).also {
		for ((my, their) in this.params.zip(it.params)) {
			their.value = my.value
		}
	}

}