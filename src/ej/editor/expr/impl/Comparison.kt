package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.expr.lists.SimpleExpressionChooser
import ej.editor.external.FunctionDecl
import ej.editor.external.Stdlib
import ej.editor.utils.EnumChooser
import ej.editor.utils.weakListenerN
import ej.editor.utils.weakListeners
import ej.utils.addToList
import javafx.scene.layout.Pane
import tornadofx.*

data class Comparison(
		var left: ExpressionBuilder?=null,
		var op: Operator = Operator.EQ,
		var right: ExpressionBuilder?=null
) : ExpressionBuilder() {
	override fun name() = "Compare values"
	override fun copyMe() = copy()
	
	private var enumMode = false
	override fun editorBody(): Pane = defaultEditorTextFlow {
		val vlLeft = valueLink(::left, "Value1", AnyExprChooser)
		text(" ")
		val vlOp = valueLink(::op, "Comparison operator",
		                     EnumChooser(Operator::longName)) {
			it?.shortName ?: "<Operator>"
		}
		text(" ")
		val vlRight = valueLink(::right, "Value2", AnyExprChooser)
		fun resetEnumMode() {
			if (enumMode) {
				enumMode = false
				vlOp.chooser = EnumChooser(Operator::longName)
				vlRight.chooser = AnyExprChooser
			}
		}
		weakListenerN(vlLeft.valueProperty, vlOp.valueProperty) { left, op ->
			val enum = ((left as? ExternalFunctionBuilder)?.decl as? FunctionDecl)?.returnTypeRaw?.let {
				Stdlib.enumByTypeName(it)
			}
			if (enum != null && (op == Operator.EQ || op == Operator.NEQ)) {
				if (!enumMode) {
					vlOp.chooser = EnumChooser(listOf(Operator.EQ, Operator.NEQ), Operator::longName)
				}
				enumMode = true
				vlRight.chooser = SimpleExpressionChooser(listOf(ExternalEnumBuilder(enum)), enum.name)
			} else {
				resetEnumMode()
			}
		}.addToList(weakListeners)
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
				is ExternalEnumBuilder ->
					l.enumDecl.name
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