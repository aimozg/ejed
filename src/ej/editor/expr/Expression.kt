package ej.editor.expr

import ej.utils.lessThan
import ej.utils.lessThanOrEqualTo
import ej.utils.toJsString
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

sealed class Expression {
	abstract val source:String
	abstract val parts:List<Expression>
	override fun toString() = source
}
class Identifier(val value:String):Expression() {
	override val parts get() = emptyList<Expression>()
	override val source get() = value
}
class IntLiteral(val value:Int):Expression() {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toString()
}
class FloatLiteral(val value:Double):Expression() {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toString()
}
class StringLiteral(val value:String):Expression() {
	override val parts get() = emptyList<Expression>()
	override val source get() = value.toJsString()
}
enum class Operator(val repr:String,val priority:Int,vararg val aliases:String) {
	OR("or",10,"||"),
	AND("and",20,"&&"),
	LT("<",30,"lt"),
	LTE("<=",30,"lte","le","leq"),// ≤
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

class ListExpression(override val parts:List<Expression>):Expression() {
	override val source get() = "["+parts.joinToString(", ")+"]"
}
class ObjectExpression(val items:Map<String,Expression>):Expression() {
	override val parts get() = items.values.toList()
	override val source get() = "{ " + items.entries.joinToString(", ") {
		(k,v) -> k.toJsString() +": "+v
	}+" }"
}
class CallExpression(val function:Expression,val arguments:List<Expression>):Expression() {
	override val parts get() = listOf(function)+arguments
	override val source get() = ""+function + "(" + arguments.joinToString(",") + ")"
}
class DotExpression(val obj:Expression,val key:String):Expression() {
	override val parts get() = listOf(obj)
	override val source get() = "$obj.$key"
}
class AccessExpression(val obj: Expression,val index:Expression):Expression() {
	override val parts get() = listOf(obj,index)
	override val source get() = "$obj[$index]"
}
class ConditionalExpression(val condition:Expression,
                            val ifTrue:Expression,
                            val ifFalse:Expression):Expression() {
	override val parts get() = listOf(condition,ifTrue,ifFalse)
	override val source get() = "$condition ? $ifTrue : $ifFalse"
}
class BinaryExpression(val left:Expression,val op:Operator,val right:Expression):Expression() {
	override val parts get() = listOf(left,right)
	override val source get(): String {
		return (if (wrapLeft()) "($left)" else left.toString())+
				op.repr+
				(if (wrapRight()) "($right)" else right.toString())
	}
	
	fun wrapLeft() = (left as? BinaryExpression)?.op?.priority?.lessThan(op.priority) == true
	fun wrapRight() = (right as? BinaryExpression)?.op?.priority?.lessThanOrEqualTo(op.priority) == true
}
class InvalidExpression(override val source:String):Expression() {
	override val parts get() = emptyList<Expression>()
}

open class ExpressionProperty(initialValue:Expression = InvalidExpression("")) : SimpleObjectProperty<Expression>(initialValue) {
	val sourceProperty = object: SimpleStringProperty() {
		override fun setValue(v: String) {
			this@ExpressionProperty.set(parseExpressionSafe(v))
		}
		
		init {
			bind(this@ExpressionProperty.stringBinding{
				it?.source?:""
			})
		}
	}
}


