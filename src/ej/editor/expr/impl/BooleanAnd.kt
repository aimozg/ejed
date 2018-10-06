package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.BoolExprChooser
import javafx.scene.layout.Pane
import tornadofx.*

data class BooleanAnd(
		var expr1: ExpressionBuilder?,
		var expr2: ExpressionBuilder?
) : ExpressionBuilder() {
	override fun copyMe() = copy()
	
	constructor() : this(null, null)
	
	override fun name() = "AND (both conditions are true)"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		valueLink(::expr1, "Condition1", BoolExprChooser)
		text(" and ")
		valueLink(::expr2, "Condition2", BoolExprChooser)
	}
	
	override fun text() = mktext("(", expr1, " and ", expr2, ")")
	
	override fun build(): Expression {
		return BinaryExpression(expr1?.build() ?: nop(),
		                        BinaryOperator.AND,
		                        expr2?.build() ?: nop())
	}
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression) =
				if (expr.op != BinaryOperator.AND) null
				else BooleanAnd().apply {
					expr1 = converter.convert(expr.left, ExpressionTypes.BOOLEAN)
					expr2 = converter.convert(expr.right, ExpressionTypes.BOOLEAN)
				}
		
	}
}