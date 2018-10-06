package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.external.ExternalFunctionBuilder
import ej.editor.expr.external.Stdlib
import ej.editor.expr.impl.*

object AnyExprChooser : ExpressionChooser() {
	override fun list() = Stdlib.functions.map {
		ExternalFunctionBuilder(it) as ExpressionBuilder
	} +
			listOf(
					// constants
					ConstInt(),
					ConstPlayer(),
					// specifics
					ModVariableBuilder(),
					CreatureStat(),
					// generics
					BooleanAnd(),
					BooleanOr(),
					Comparison(),
					RawExpressionBuilder()
			)
	
}