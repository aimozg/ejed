package ej.editor.expr

object AnyExprChooser : ExpressionChooser() {
	override fun pickValue(initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(initial,listOf(
				ModVariableBuilder(),
				ConstInt()
		))
	}
	
}