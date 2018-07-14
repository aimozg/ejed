package ej.editor.expr

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class BooleanAnd : ExpressionBuilder() {
	override fun name() = "AND (complex condition)"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		valueLink(expr1, BoolExprChooser, "<Condition1>")
		text(" and ")
		valueLink(expr2, BoolExprChooser, "<Condition2>")
	}
	override fun text() = mktext(expr1," and ",expr2)
	
	override fun build(): Expression {
		return BinaryExpression(expr1.value.build(),
		                        BinaryOperator.AND,
		                        expr2.value.build())
	}
	
	val expr1 = SimpleObjectProperty<ExpressionBuilder>()
	val expr2 = SimpleObjectProperty<ExpressionBuilder>()
	
}

enum class ComparisonOp(val readableName:String, val bop: BinaryOperator) : WithReadableText {
	EQ("is equal to", BinaryOperator.EQ),
	NEQ("is not equal to", BinaryOperator.NEQ),
	GT("is greater than", BinaryOperator.GT),
	GTE("is greater than or equal to", BinaryOperator.GTE),
	LT("is less than", BinaryOperator.LT),
	LTE("is less than or equal to", BinaryOperator.LTE);
	
	override fun text() = readableName
}

class Comparison : ExpressionBuilder() {
	override fun name() = "Compare values"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		valueLink(left, AnyExprChooser, "<Value1>")
		text(" ")
		valueLink(op,
		          EnumChooser(ComparisonOp.values(),
		                      ComparisonOp::readableName)) {
			it?.readableName ?: "<Operation>"
		}
		text(" ")
		valueLink(right, AnyExprChooser, "<Value2>")
	}
	
	override fun text() = mktext(left," ",op," ",right)
	override fun build() = BinaryExpression(left.value.build(), op.value.bop, right.value.build())
	
	val left = SimpleObjectProperty<ExpressionBuilder>()
	val op = SimpleObjectProperty<ComparisonOp>(ComparisonOp.EQ)
	val right = SimpleObjectProperty<ExpressionBuilder>()
}