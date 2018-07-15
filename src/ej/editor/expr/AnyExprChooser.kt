package ej.editor.expr

import ej.editor.expr.impl.ConstInt
import ej.editor.expr.impl.ModVariableBuilder
import ej.editor.expr.impl.RawExpressionBuilder

object AnyExprChooser : ExpressionChooser() {
	override fun pickValue(initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(initial,listOf(
				ModVariableBuilder(),
				ConstInt(),
				RawExpressionBuilder()
		))
	}
	
}