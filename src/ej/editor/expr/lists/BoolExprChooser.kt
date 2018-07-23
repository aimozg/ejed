package ej.editor.expr.lists

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionChooser
import ej.editor.expr.external.ExternalFunctionBuilder
import ej.editor.expr.external.Stdlib
import ej.editor.expr.impl.BooleanAnd
import ej.editor.expr.impl.BooleanOr
import ej.editor.expr.impl.Comparison
import ej.editor.expr.impl.RawExpressionBuilder

object BoolExprChooser : ExpressionChooser() {
	override fun pickValue(title:String,initial: ExpressionBuilder?): ExpressionBuilder? {
		return pickFromList(title,initial,
		                    Stdlib.functionsReturning("boolean").map {
			                    ExternalFunctionBuilder(it) as ExpressionBuilder
		                    } + listOf(
				// specifics
//				CreatureSexTest(),
				// generics
				BooleanAnd(),
				BooleanOr(),
				Comparison(),
				RawExpressionBuilder()
		))
	}
	
}