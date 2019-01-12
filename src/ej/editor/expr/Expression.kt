package ej.editor.expr

import ej.utils.lessThan
import ej.utils.lessThanOrEqualTo
import ej.utils.toJsString

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

sealed class Expression {
	abstract val source:String
	abstract val parts:List<Expression>
	override fun toString() = source
	abstract fun copy():Expression
	final override fun equals(other: Any?) = (other as? Expression)?.source == source
	final override fun hashCode() = source.hashCode()
}
class Identifier(val value:String):Expression() {
	override val parts get() = emptyList<Expression>()
	override val source get() = value
	override fun copy() = Identifier(value)
	
	companion object {
		fun valueOf(source: String): Identifier =
				RESERVED_LITERALS.find { it.value == source } ?: Identifier(source)
		
		val True = Identifier("true")
		val False = Identifier("false")
		val Null = Identifier("null")
		
		val RESERVED_LITERALS = listOf(True,False,Null)
	}
}
sealed class ConstLiteral<T:Any?>(val value:T):Expression()
class IntLiteral(value:Int):ConstLiteral<Int>(value) {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toString()
	override fun copy() = IntLiteral(value)
}
class FloatLiteral(value:Double):ConstLiteral<Double>(value) {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toString()
	override fun copy() = FloatLiteral(value)
}
class StringLiteral(value:String):ConstLiteral<String>(value) {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toJsString(preferSingleQuote = true)
	override fun copy() = StringLiteral(value)
}
enum class BinaryOperator(val repr:String, val priority:Int, vararg val aliases:String) {
	OR("or",10,"||"),
	AND("and",20,"&&"),
	LT("lt", 30, "<"),
	LTE("lte", 30, "<=", "le", "leq"),// ≤
	GT("gt", 30, ">"),
	GTE("gte", 30, ">=", "ge", "geq"), // ≥
	NEQ("!=",30,"ne","neq","!=="), // ≠
	EQ("==",30,"eq","=","==="),
	ADD("+",40),
	SUB("-",40),
	MUL("*",50),
	DIV("/",50),
	MOD("mod",50);
	companion object {
		const val PRIORITY_ABOVE_ALL = 60
		fun parse(s:String):BinaryOperator? = values().firstOrNull {
			it.repr == s || s in it.aliases
		}
	}
}

class ListExpression(override val parts:List<Expression>):Expression() {
	override val source get() = "["+parts.joinToString(", ")+"]"
	override fun copy() = ListExpression(parts.map(Expression::copy))
}
class ObjectExpression(val items:Map<String,Expression>):Expression() {
	override val parts get() = items.values.toList()
	override val source get() = "{ " + items.entries.joinToString(", ") {
		(k,v) -> k.toJsString() +": "+v
	}+" }"
	override fun copy() = ObjectExpression(items.mapValues{it.value.copy()})
}
class CallExpression(val function:Expression,val arguments:List<Expression>):Expression() {
	override val parts get() = listOf(function)+arguments
	override val source get() = ""+function + "(" + arguments.joinToString(",") + ")"
	override fun copy() = CallExpression(function.copy(),arguments.map(Expression::copy))
}
class DotExpression(val obj:Expression,val key:String):Expression() {
	override val parts get() = listOf(obj)
	override val source get() = "$obj.$key"
	override fun copy() = DotExpression(obj.copy(),key)
}
class AccessExpression(val obj: Expression,val index:Expression):Expression() {
	override val parts get() = listOf(obj,index)
	override val source get() = "$obj[$index]"
	override fun copy() = AccessExpression(obj.copy(),index.copy())
}
class ConditionalExpression(val condition:Expression,
                            val ifTrue:Expression,
                            val ifFalse:Expression):Expression() {
	override val parts get() = listOf(condition,ifTrue,ifFalse)
	override val source get() = "$condition ? $ifTrue : $ifFalse"
	override fun copy() = ConditionalExpression(condition.copy(), ifTrue.copy(), ifFalse.copy())
}

class BooleanNotExpression(val expr: Expression) : Expression() {
	override val parts get() = listOf(expr)
	override val source get() = "not $expr"
	override fun copy() = BooleanNotExpression(expr.copy())
}
class BinaryExpression(val left:Expression, val op:BinaryOperator, val right:Expression):Expression() {
	override val parts get() = listOf(left,right)
	override val source get(): String {
		return (if (wrapLeft()) "($left)" else left.toString())+
				" "+op.repr+" "+
				(if (wrapRight()) "($right)" else right.toString())
	}
	override fun copy() = BinaryExpression(left.copy(), op, right.copy())
	
	fun wrapLeft() = (left as? BinaryExpression)?.op?.priority?.lessThan(op.priority) == true
	fun wrapRight() = (right as? BinaryExpression)?.op?.priority?.lessThanOrEqualTo(op.priority) == true
}
class InvalidExpression(override val source:String):Expression() {
	override val parts get() = emptyList<Expression>()
	override fun copy() = InvalidExpression(source)
}


