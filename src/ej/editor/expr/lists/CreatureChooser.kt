package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.impl.ConstPlayer
import ej.editor.expr.impl.RawExpressionBuilder

object CreatureChooser : ExpressionChooser() {
	override fun pickValue(title: String, initial: ExpressionBuilder?) = pickFromList(title, initial, listOf(
			// specifics
			ConstPlayer(),
			// generics
			RawExpressionBuilder()
	))
	
	
}