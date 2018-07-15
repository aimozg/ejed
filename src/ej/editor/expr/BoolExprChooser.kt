package ej.editor.expr

import ej.editor.expr.impl.BooleanAnd
import ej.editor.expr.impl.Comparison
import ej.editor.expr.impl.RawExpressionBuilder

object BoolExprChooser : ExpressionChooser() {
	override fun pickValue(initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(initial,listOf(
				BooleanAnd(),
				Comparison(),
				RawExpressionBuilder()
		))
	}
	
}