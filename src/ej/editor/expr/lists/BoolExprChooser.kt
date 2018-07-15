package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.impl.*

object BoolExprChooser : ExpressionChooser() {
	override fun pickValue(initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(initial,listOf(
				// specifics
				CreatureSexTest(),
				// generics
				BooleanAnd(),
				BooleanOr(),
				Comparison(),
				RawExpressionBuilder()
		))
	}
	
}