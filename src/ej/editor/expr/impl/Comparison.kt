package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.AnyExprChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class Comparison : ExpressionBuilder() {
	override fun name() = "Compare values"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		valueLink("Value1", left, AnyExprChooser)
		text(" ")
		valueLink("Comparison operator", op,
		          EnumChooser(Operator::longName)) {
			it?.shortName ?: "<Operator>"
		}
		text(" ")
		valueLink("Value2", right, AnyExprChooser)
	}
	
	override fun text() = mktext("(", left, " ", op, " ", right, ")")
	override fun build() = BinaryExpression(left.value?.build() ?: nop(),
	                                        op.value.bop,
	                                        right.value?.build() ?: nop())
	
	val left = SimpleObjectProperty<ExpressionBuilder?>()
	val op = SimpleObjectProperty<Operator>(Operator.EQ)
	val right = SimpleObjectProperty<ExpressionBuilder?>()
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression): Comparison? = Comparison().apply {
			op.value = (Operator.byOperator(expr.op) ?: return null)
			left.value = converter.convert(expr.left)
			right.value = converter.convert(expr.right)
		}
	}
	
	enum class Operator(val shortName: String, val longName: String, val bop: BinaryOperator) : WithReadableText {
		EQ("=", "is equal to", BinaryOperator.EQ),
		NEQ("≠", "is not equal to", BinaryOperator.NEQ),
		GT(">", "is greater than", BinaryOperator.GT),
		GTE("≥", "is greater than or equal to", BinaryOperator.GTE),
		LT("<", "is less than", BinaryOperator.LT),
		LTE("≤", "is less than or equal to", BinaryOperator.LTE);
		
		override fun text() = shortName
		
		companion object {
			fun byOperator(bop: BinaryOperator) = values().firstOrNull { it.bop == bop }
		}
	}
}