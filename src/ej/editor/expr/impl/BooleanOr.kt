package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.BoolExprChooser
import javafx.scene.layout.Pane
import tornadofx.*

data class BooleanOr(
		var expr1: ExpressionBuilder? = null,
		var expr2: ExpressionBuilder? = null
) : ExpressionBuilder() {
	override fun copyMe() = copy()
	override fun name() = "OR (either of conditions is true)"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		valueLink(::expr1, "Condition1", BoolExprChooser)
		text(" or ")
		valueLink(::expr2, "Condition2", BoolExprChooser)
	}
	override fun text() = mktext("(",expr1," or ",expr2,")")
	
	override fun build(): Expression {
		return BinaryExpression(expr1?.build()?: nop(),
		                        BinaryOperator.OR,
		                        expr2?.build()?:nop())
	}
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression) =
				if (expr.op != BinaryOperator.OR) null
				else BooleanOr().apply {
					expr1 = converter.convert(expr.left)
					expr2 = converter.convert(expr.right)
				}
		
	}
}