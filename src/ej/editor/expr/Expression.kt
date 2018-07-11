package ej.editor.expr

import ej.utils.lessThan
import ej.utils.lessThanOrEqualTo
import ej.utils.toJsString

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

sealed class Expression {

}
data class Identifier(val value:String):Expression() {
	override fun toString() = value
}
data class IntLiteral(val value:Int):Expression() {
	override fun toString() = value.toString()
}
data class FloatLiteral(val value:Double):Expression() {
	override fun toString() = value.toString()
}
data class StringLiteral(val value:String):Expression() {
	override fun toString() = value.toJsString()
}
enum class Operator(val repr:String,val priority:Int,vararg val aliases:String) {
	OR("or",10,"||"),
	AND("and",20,"&&"),
	LT("<",30,"lt"),
	LTE("<=",30,"lte","le","leq"),//≤
	GT(">",30,"gt"),
	GTE(">=",30,"gte","ge","geq"), // ≥
	NEQ("!=",30,"ne","neq","!=="), // ≠
	EQ("==",30,"eq","=","==="),
	ADD("+",40),
	SUB("-",40),
	MUL("*",50),
	DIV("/",50),
	MOD("mod",50);
	companion object {
		fun parse(s:String):Operator? = values().firstOrNull {
			it.repr == s || s in it.aliases
		}
	}
}

data class ListExpression(val items:List<Expression>):Expression() {
	override fun toString() = "["+items.joinToString(", ")+"]"
}
data class ObjectExpression(val items:Map<String,Expression>):Expression() {
	override fun toString() = "{ " + items.entries.joinToString(", ") {
		(k,v) -> k.toJsString() +": "+v
	}+" }"
}
data class CallExpression(val function:Expression,val arguments:List<Expression>):Expression() {
	override fun toString() = ""+function + "(" + arguments.joinToString(",") + ")"
}
data class DotExpression(val obj:Expression,val key:String):Expression() {
	override fun toString() = "$obj.$key"
}
data class AccessExpression(val obj:Expression,val index:Expression):Expression() {
	override fun toString() = "$obj[$index]"
}
data class ConditionalExpression(val condition:Expression,
                            val ifTrue:Expression,
                            val ifFalse:Expression):Expression() {
	override fun toString() = "$condition ? $ifTrue : $ifFalse"
}
data class BinaryExpression(val left:Expression,val op:Operator,val right:Expression):Expression() {
	override fun toString(): String {
		return (if (wrapLeft()) "($left)" else left.toString())+
				op.repr+
				(if (wrapRight()) "($right)" else right.toString())
	}
	
	fun wrapLeft() = (left as? BinaryExpression)?.op?.priority?.lessThan(op.priority) == true
	fun wrapRight() = (right as? BinaryExpression)?.op?.priority?.lessThanOrEqualTo(op.priority) == true
}