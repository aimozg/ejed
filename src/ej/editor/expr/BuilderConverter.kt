package ej.editor.expr

import ej.editor.expr.external.ExternalEnumBuilder
import ej.editor.expr.external.Stdlib
import ej.editor.expr.impl.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */

interface BuilderConverter {
	fun convert(expr: Expression, type: String): ExpressionBuilder
}

interface PartialBuilderConverter<E : Expression> {
	fun tryConvert(converter: BuilderConverter, expr: E): ExpressionBuilder?
}

object DefaultCommandConverter : BuilderConverter {
	override fun convert(expr: Expression, type: String): ExpressionBuilder {
		if (type == ExpressionTypes.VOID) {
			val command = if (expr is CallExpression) Stdlib.tryConvertCommand(DefaultBuilderConverter, expr) else null
			if (command != null) return command
		}
		return RawExpressionBuilder(expr)
	}
}
object DefaultBuilderConverter : BuilderConverter {
	override fun convert(expr: Expression,type:String): ExpressionBuilder {
		if (type == ExpressionTypes.VOID) {
			return DefaultCommandConverter.convert(expr, type)
		}
		val enumDecl = Stdlib.enumByTypeName(type)
		val enumConstDecl = enumDecl?.valueByImpl(expr.source)
		if (enumConstDecl != null) {
			return ExternalEnumBuilder(enumDecl, enumConstDecl)
		}
		return when (expr) {
			is Identifier ->
				ConstPlayer.tryConvert(this, expr)
						?: ConstBool.tryConvert(this, expr)
			is IntLiteral ->
				ConstInt.tryConvert(this, expr)
			is FloatLiteral -> null
			is StringLiteral -> null
			is ListExpression -> null
			is ObjectExpression -> null
			is CallExpression ->
				Stdlib.tryConvert(this, expr)
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
	
}