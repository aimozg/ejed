package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.BoolExprChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class BooleanOr : ExpressionBuilder() {
	override fun name() = "OR (either of conditions is true)"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		valueLink(expr1, "Condition1", BoolExprChooser)
		text(" or ")
		valueLink(expr2, "Condition2", BoolExprChooser)
	}
	override fun text() = mktext("(",expr1," or ",expr2,")")
	
	override fun build(): Expression {
		return BinaryExpression(expr1.value.build(),
		                        BinaryOperator.OR,
		                        expr2.value.build())
	}
	
	val expr1 = SimpleObjectProperty<ExpressionBuilder>()
	val expr2 = SimpleObjectProperty<ExpressionBuilder>()
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression) =
				if (expr.op != BinaryOperator.OR) null
				else BooleanOr().apply {
					expr1.value = converter.convert(expr.left)
					expr2.value = converter.convert(expr.right)
				}
		
	}
}