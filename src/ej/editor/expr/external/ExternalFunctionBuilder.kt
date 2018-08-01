package ej.editor.expr.external

import ej.editor.expr.*
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.lists.CreatureChooser
import ej.editor.expr.lists.SimpleExpressionChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.text.TextFlow
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
			if (r != null) paramsByName[r.name]
			else l
		})
	}

	// TODO if impl present
	override fun build() = CallExpression(
			Identifier(decl.name),
			params.map { it.value?.build()?: nop() }
	)

	override fun editorBody() = defaultEditorTextFlow {
		for ((ltext, rparam) in decl.editor.parts) {
			val decl = rparam?.decl
			val prop = paramsByName[rparam?.name]
			if (rparam != null && decl != null && prop != null) {
				when (rparam.type) {
					ExpressionEditorDecl.ParamEditorType.SELECT -> {
						if (decl.type == "Perk") {
							linkFor(decl, prop)
							/*
							TODO list of perks
							val values = Natives.perks.map {
							
							}
							combobox(prop, ) {
							
							}
							 */
						} else {
							val enumDecl = Stdlib.enumByTypeName(decl.type)
							if (enumDecl != null) {
								val values = enumDecl.values.map {
									ExternalEnumBuilder(enumDecl, it)
								}
								combobox(prop, values) {
									cellFormat(DefaultScope) { eb ->
										text = enumDecl.values.find {
											it.impl == eb?.build()?.source
										}?.name ?: "<???>"
									}
								}
							} else {
								linkFor(decl, prop)
							}
						}
					}
					ExpressionEditorDecl.ParamEditorType.INPUT -> TODO()
					ExpressionEditorDecl.ParamEditorType.CHECKBOX -> TODO()
					ExpressionEditorDecl.ParamEditorType.LINK ->
						linkFor(decl, prop)
				}
				
			}
			if(ltext != null) text(ltext)
		}
	}
	
	fun TextFlow.linkFor(decl: ParamDecl,
	                     prop: SimpleObjectProperty<ExpressionBuilder?>) {
		val chooser: ExpressionChooser = when (decl.type) {
			"boolean" -> BoolExprChooser
	//					"int" -> IntExprChooser
	//					"float" -> FloatExprChooser
	//					"string" -> TextExprChooser
			"Creature" -> CreatureChooser
			// "Perk" -> TODO() // PerkChooser
			/* TODO pick proper chooser*/
			else -> {
				val list = Stdlib.buildersReturning(decl.type)
				if (list.isNotEmpty()) {
					SimpleExpressionChooser(list)
				} else {
					AnyExprChooser
				}
			}
		}
		valueLink(prop, decl.name, chooser)
	}
	
	override fun name() = decl.listname ?: decl.name

	override fun copyMe() = ExternalFunctionBuilder(decl).also {
		for ((my, their) in this.params.zip(it.params)) {
			their.value = my.value
		}
	}

}