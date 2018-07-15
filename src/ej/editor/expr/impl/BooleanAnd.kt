package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.BoolExprChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class BooleanAnd : ExpressionBuilder() {
	override fun name() = "AND (both conditions are true)"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		valueLink("Condition1",expr1, BoolExprChooser)
		text(" and ")
		valueLink("Condition2", expr2, BoolExprChooser)
	}
	override fun text() = mktext("(",expr1," and ",expr2,")")
	
	override fun build(): Expression {
		return BinaryExpression(expr1.value.build(),
		                        BinaryOperator.AND,
		                        expr2.value.build())
	}
	
	val expr1 = SimpleObjectProperty<ExpressionBuilder>()
	val expr2 = SimpleObjectProperty<ExpressionBuilder>()
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression) =
				if (expr.op != BinaryOperator.AND) null
				else BooleanAnd().apply {
					expr1.value = converter.convert(expr.left)
					expr2.value = converter.convert(expr.right)
				}
		
	}
}