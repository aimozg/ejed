package ej.editor.expr.external

import ej.editor.expr.*
import ej.editor.expr.impl.ConstBool
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.lists.CreatureChooser
import ej.editor.expr.lists.SimpleExpressionChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.text.FontPosture
import javafx.scene.text.TextFlow
import tornadofx.*

/*
 * Created by aimozg on 18.07.2018.
 * Confidential unless published on GitHub
 */

class ExternalFunctionBuilder(
		val decl: ExpressionDecl
) : ExpressionBuilder() {
	val params = decl.params.map { param ->
		val d = param.default?.let {
			DefaultBuilderConverter.convert(parseExpressionSafe(it), param.type)
		}
		SimpleObjectProperty(this, param.name, d)
	}
	val paramsByName = params.associateBy { it.name }

	@Suppress("IMPLICIT_CAST_TO_ANY")
	override fun text(): String {
		return mktext(decl.editor.parts.map {(l,r)->
			if (r != null) {
				val expr = paramsByName[r.name]?.value
				when (r.type) {
					ExpressionEditorDecl.ParamEditorType.CHECKBOX ->
						if (expr is ConstBool) {
							if (expr.constant.value == true) r.typedata
							else ""
						} else {
							expr
						}
					else -> expr
				}
			} else {
				l?.replace("\\n", "\n")
			}
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
					ExpressionEditorDecl.ParamEditorType.SELECT ->
						selectFor(decl, prop)
					ExpressionEditorDecl.ParamEditorType.INPUT ->
						inputFor(decl, prop)
					ExpressionEditorDecl.ParamEditorType.CHECKBOX ->
						checkboxFor(decl, prop, rparam.typedata)
					ExpressionEditorDecl.ParamEditorType.LINK ->
						linkFor(decl, prop)
				}
				
			}
			if(ltext != null) text(ltext)
		}
		decl.editorHint?.let { hint ->
			text("\n" + hint) {
				style {
					padding = box(0.5.em)
					fontStyle = FontPosture.ITALIC
				}
			}
		}
	}
	
	private fun TextFlow.inputFor(decl: ParamDecl,
	                              prop: SimpleObjectProperty<ExpressionBuilder?>) {
		TODO()
	}
	
	private fun TextFlow.checkboxFor(decl: ParamDecl,
	                                 prop: SimpleObjectProperty<ExpressionBuilder?>,
	                                 typedata: String?) {
		val pv = prop.value
		if (decl.type == ExpressionTypes.BOOLEAN && pv is ConstBool) {
			checkbox(typedata, pv.constant)
		} else {
			linkFor(decl, prop)
		}
	}
	
	private fun TextFlow.selectFor(decl: ParamDecl,
	                               prop: SimpleObjectProperty<ExpressionBuilder?>) {
		if (decl.type == ExpressionTypes.PERK) {
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
	
	fun TextFlow.linkFor(decl: ParamDecl,
	                     prop: SimpleObjectProperty<ExpressionBuilder?>) {
		val chooser: ExpressionChooser = when (decl.type) {
			ExpressionTypes.BOOLEAN -> BoolExprChooser
			//	ExpressionTypes.INT -> IntExprChooser
			//	ExpressionTypes.FLOAT -> FloatExprChooser
			//	ExpressionTypes.STRING -> TextExprChooser
			ExpressionTypes.CREATURE -> CreatureChooser
			// ExpressionTypes.PERK -> TODO() // PerkChooser
			/* TODO pick proper chooser*/
			else -> {
				val list = Stdlib.buildersReturning(decl.type)
				if (list.isNotEmpty()) {
					SimpleExpressionChooser(list, decl.type)
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
	
	override fun initializableBy(initial: ExpressionBuilder): Boolean {
		return (initial as? ExternalFunctionBuilder)?.decl == decl
	}
}