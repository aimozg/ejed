package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.ExpressionTypes

/*
 * Created by aimozg on 23.07.2018.
 * Confidential until published on GitHub
 */
class SimpleExpressionChooser(
		val items: List<ExpressionBuilder>,
		override val expressionType: String = ExpressionTypes.ANY
) : ExpressionChooser() {
	override fun list() = items
	
}