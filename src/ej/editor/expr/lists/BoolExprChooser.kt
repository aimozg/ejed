package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.ExpressionTypes
import ej.editor.expr.impl.*
import ej.editor.external.Stdlib

object BoolExprChooser : ExpressionChooser() {
	override fun list(): List<ExpressionBuilder> =
			Stdlib.buildersReturning(ExpressionTypes.BOOLEAN) + listOf(
					// specifics
					// generics
					BooleanAnd(),
					BooleanOr(),
					BooleanNot(),
					Comparison(),
					RawExpressionBuilder()
			)
	
	override val expressionType: String get() = ExpressionTypes.BOOLEAN
	
}