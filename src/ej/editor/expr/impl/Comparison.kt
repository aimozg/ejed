package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.external.FunctionDecl
import ej.editor.utils.EnumChooser
import javafx.scene.layout.Pane
import tornadofx.*

data class Comparison(
		var left: ExpressionBuilder?=null,
		var op: Operator = Operator.EQ,
		var right: ExpressionBuilder?=null
) : ExpressionBuilder() {
	override fun name() = "Compare values"
	override fun copyMe() = copy()
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		valueLink(::left, "Value1", AnyExprChooser)
		text(" ")
		valueLink(::op, "Comparison operator",
		          EnumChooser(Operator::longName)) {
			it?.shortName ?: "<Operator>"
		}
		text(" ")
		valueLink(::right, "Value2", AnyExprChooser)
	}
	
	override fun text() = mktext("(", left, " ", op, " ", right, ")")
	override fun build() = BinaryExpression(left?.build() ?: nop(),
	                                        op.bop,
	                                        right?.build() ?: nop())
	
	companion object : PartialBuilderConverter<BinaryExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: BinaryExpression): Comparison? = Comparison().apply {
			op = (Operator.byOperator(expr.op) ?: return null)
			val l = converter.convert(expr.left, ExpressionTypes.ANY)
			left = l
			val rtype = when (l) {
				is ExternalFunctionBuilder ->
					(l.decl as? FunctionDecl?)?.returnTypeRaw
							?: ExpressionTypes.ANY
				else -> ExpressionTypes.ANY
			}
			right = converter.convert(expr.right, rtype)
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