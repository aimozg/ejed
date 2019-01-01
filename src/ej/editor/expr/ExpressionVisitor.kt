package ej.editor.expr

/*
 * Created by aimozg on 18.07.2018.
 * Confidential until published on GitHub
 */

abstract class ExpressionVisitor() {
	open fun visitAnyExpression(x:Expression) {}
	open fun visitAllExpressions(parts:Collection<Expression>) {
		for (part in parts) {
			part.visit(this)
		}
	}
	
	open fun visitIdentifier(x:Identifier) {
		visitAnyExpression(x)
	}
	open fun visitAnyConst(x:ConstLiteral<*>) {
		visitAnyExpression(x)
	}
	open fun visitIntLiteral(x:IntLiteral) {
		visitAnyConst(x)
	}
	open fun visitFloatLiteral(x:FloatLiteral) {
		visitAnyConst(x)
	}
	open fun visitStringLiteral(x:StringLiteral) {
		visitAnyConst(x)
	}
	open fun visitList(x:ListExpression) {
		visitAnyExpression(x)
		visitAllExpressions(x.parts)
	}
	open fun visitObject(x:ObjectExpression) {
		visitAnyExpression(x)
		visitAllExpressions(x.items.values)
	}
	open fun visitCall(x:CallExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.function)
		visitAllExpressions(x.arguments)
	}
	open fun visitDot(x:DotExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.obj)
	}
	open fun visitAccess(x:AccessExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.obj)
		visitAnyExpression(x.index)
	}
	open fun visitConditional(x:ConditionalExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.condition)
		visitAnyExpression(x.ifTrue)
		visitAnyExpression(x.ifFalse)
	}
	open fun visitBinary(x:BinaryExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.left)
		visitAnyExpression(x.right)
	}
	
	open fun visitBooleanNot(x: BooleanNotExpression) {
		visitAnyExpression(x)
		visitAnyExpression(x.expr)
	}
	open fun visitInvalid(x:InvalidExpression) {
		visitAnyExpression(x)
	}
	
}

fun Expression.visit(visitor: ExpressionVisitor):Unit =
	when (this) {
		is Identifier -> visitor.visitIdentifier(this)
		is ConstLiteral<*> -> when(this) {
			is IntLiteral -> visitor.visitIntLiteral(this)
			is FloatLiteral -> visitor.visitFloatLiteral(this)
			is StringLiteral -> visitor.visitStringLiteral(this)
		}
		is ListExpression -> visitor.visitList(this)
		is ObjectExpression -> visitor.visitObject(this)
		is CallExpression -> visitor.visitCall(this)
		is DotExpression -> visitor.visitDot(this)
		is AccessExpression -> visitor.visitAccess(this)
		is ConditionalExpression -> visitor.visitConditional(this)
		is BooleanNotExpression -> visitor.visitBooleanNot(this)
		is BinaryExpression -> visitor.visitBinary(this)
		is InvalidExpression -> visitor.visitInvalid(this)
	}
