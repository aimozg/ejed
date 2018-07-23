package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser

/*
 * Created by aimozg on 23.07.2018.
 * Confidential until published on GitHub
 */
class SimpleExpressionChooser(
		val items: List<ExpressionBuilder>
) : ExpressionChooser() {
	override fun pickValue(title: String, initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(title, initial, items)
	}
	
}