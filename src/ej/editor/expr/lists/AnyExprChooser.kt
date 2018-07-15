package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.impl.*

object AnyExprChooser : ExpressionChooser() {
	override fun pickValue(title:String, initial: ExpressionBuilder?) = pickFromList(title, initial, listOf(
			// constants
			ConstInt(),
			ConstPlayer(),
			// specifics
			ModVariableBuilder(),
			CreatureSexTest(),
			// generics
			BooleanAnd(),
			BooleanOr(),
			Comparison(),
			RawExpressionBuilder()
	))
	
}