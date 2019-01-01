package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.BoolExprChooser
import javafx.scene.layout.Pane
import tornadofx.*

data class BooleanNot(
		var expr1: ExpressionBuilder?
) : ExpressionBuilder() {
	override fun copyMe() = copy()
	
	constructor() : this(null)
	
	override fun name() = "NOT (true if inner condition is false)"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		text("not ")
		valueLink(::expr1, "Condition", BoolExprChooser)
	}
	
	override fun text() = mktext("(not ", expr1, ")")
	
	override fun build() = BooleanNotExpression(expr1?.build() ?: nop())
	
	companion object : PartialBuilderConverter<BooleanNotExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BooleanNotExpression): ExpressionBuilder? =
				BooleanNot(converter.convert(expr.expr, ExpressionTypes.BOOLEAN))
	}
}