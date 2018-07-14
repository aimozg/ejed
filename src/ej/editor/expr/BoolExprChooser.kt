package ej.editor.expr

object BoolExprChooser : ExpressionChooser() {
	override fun pickValue(initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(initial,listOf(
				BooleanAnd(),
				Comparison()
		))
	}
	
}