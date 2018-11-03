package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.impl.*
import ej.editor.external.Stdlib

object AnyExprChooser : ExpressionChooser() {
	override fun list() = Stdlib.functions.map {
		ExternalFunctionBuilder(it) as ExpressionBuilder
	} +
			listOf(
					// constants
					ConstInt(),
					ConstFloat(),
					ConstText(),
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