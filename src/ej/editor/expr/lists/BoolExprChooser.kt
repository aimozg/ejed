package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.impl.*

object BoolExprChooser : ExpressionChooser() {
	override fun pickValue(title:String,initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(title,initial,listOf(
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