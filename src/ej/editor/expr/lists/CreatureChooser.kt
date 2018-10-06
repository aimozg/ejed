package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.ExpressionTypes
import ej.editor.expr.impl.ConstPlayer
import ej.editor.expr.impl.RawExpressionBuilder

object CreatureChooser : ExpressionChooser() {
	override fun list(): List<ExpressionBuilder> = listOf(
			// specifics
			ConstPlayer(),
			// generics
			RawExpressionBuilder()
	)
	
	override val expressionType get() = ExpressionTypes.CREATURE
}