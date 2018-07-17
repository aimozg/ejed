package ej.editor.expr

import ej.editor.expr.impl.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */

interface BuilderConverter {
	fun convert(expr: Expression): ExpressionBuilder
}

interface PartialBuilderConverter<E : Expression> {
	fun tryConvert(converter: BuilderConverter, expr: E): ExpressionBuilder?
}

object DefaultBuilderConverter : BuilderConverter {
	override fun convert(expr: Expression) = when (expr) {
		is Identifier ->
			ConstPlayer.tryConvert(this, expr)
		is IntLiteral ->
			ConstInt.tryConvert(this, expr)
		is FloatLiteral -> null
		is StringLiteral -> null
		is ListExpression -> null
		is ObjectExpression -> null
		is CallExpression ->
			CreatureSexTest.tryConvert(this,expr)
		is DotExpression ->
			ModVariableBuilder.tryConvert(this, expr)
					?: CreatureStat.tryConvert(this, expr)
		is AccessExpression -> null
		is ConditionalExpression -> null
		is BinaryExpression ->
			Comparison.tryConvert(this, expr)
					?: BooleanAnd.tryConvert(this, expr)
					?: BooleanOr.tryConvert(this, expr)
		is InvalidExpression -> null
	} ?: RawExpressionBuilder(expr)
	
}