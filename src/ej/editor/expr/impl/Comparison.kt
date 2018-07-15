package ej.editor.expr.impl

import ej.editor.expr.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class Comparison : ExpressionBuilder() {
	override fun name() = "Compare values"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		valueLink(left, AnyExprChooser, "<Value1>")
		text(" ")
		valueLink(op,
		          EnumChooser(Operator.values(),
		                      Operator::readableName)) {
			it?.readableName ?: "<Operation>"
		}
		text(" ")
		valueLink(right, AnyExprChooser, "<Value2>")
	}
	
	override fun text() = mktext("(",left," ",op," ",right,")")
	override fun build() = BinaryExpression(left.value?.build() ?: nop(),
	                                        op.value.bop,
	                                        right.value?.build() ?: nop())
	
	val left = SimpleObjectProperty<ExpressionBuilder?>()
	val op = SimpleObjectProperty<Operator>(Operator.EQ)
	val right = SimpleObjectProperty<ExpressionBuilder?>()
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression): Comparison? = Comparison().apply {
			left.value = converter.convert(expr.left)
			op.value = (Operator.byOperator(expr.op) ?: return null)
			right.value = converter.convert(expr.right)
		}
	}
	
	enum class Operator(val readableName:String, val bop: BinaryOperator) : WithReadableText {
		EQ("is equal to", BinaryOperator.EQ),
		NEQ("is not equal to", BinaryOperator.NEQ),
		GT("is greater than", BinaryOperator.GT),
		GTE("is greater than or equal to", BinaryOperator.GTE),
		LT("is less than", BinaryOperator.LT),
		LTE("is less than or equal to", BinaryOperator.LTE);
		
		override fun text() = readableName
		companion object {
			fun byOperator(bop: BinaryOperator) = values().firstOrNull { it.bop == bop }
		}
	}
}