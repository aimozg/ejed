package ej.editor.expr

import ej.mod.StateVar
import javafx.beans.value.ObservableValue
import org.funktionale.either.Either
import org.funktionale.utils.identity
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

sealed class Value {

}
class ExpressionValue(var expr:Expression):Value() {

}
class ModVariableValue(var ref:StateVar):Value() {

}

abstract class HumanizedExpression {
	abstract fun toExpression(): Expression
	abstract fun represent():List<Either<String, Value>>
	fun toSource() = toExpression().source
	override fun toString() = represent().joinToString {
		it.fold(identity(), Value::toString)
	}
}
class HexUnknown(val wrapped:Expression):HumanizedExpression() {
	override fun toExpression() = wrapped
	override fun represent(): List<Either<String, Value>> =
			listOf(Either.right(ExpressionValue(wrapped)))
}

/*
fun Iterable<Expression>.humanizeToString() = joinToString { it.humanize() }
fun Expression.humanize():String {
	return defaultHumanize()
}
fun Expression.defaultHumanize():String {
	return when (this) {
		is Identifier -> value // TODO if known id
		is IntLiteral,
		is FloatLiteral,
		is StringLiteral -> toString()
		is ListExpression -> "(list: " + items.humanizeToString() + ")"
		is ObjectExpression -> "(object: " + items.entries.joinToString { (k, v) ->
			"$k = ${v.humanize()}"
		} + ")"
		is CallExpression -> specialHumanize() ?:
			"(call function "+function.humanize()+
				(if (arguments.isEmpty())"" else
						" with arguments: "+arguments.humanizeToString())+")"
		is DotExpression -> specialHumanize() ?:
			"(property $key of ${obj.humanize()})"
		is AccessExpression -> specialHumanize() ?:
			"(element ${index.humanize()} of ${obj.humanize()})"
		is ConditionalExpression -> "(if ${condition.humanize()} then ${ifTrue.humanize()} else ${ifFalse.humanize()})"
		is BinaryExpression ->
			(if (wrapLeft()) "(${left.humanize()})" else left.humanize())+
					" "+op.repr+" "+
					(if (wrapRight()) "(${right.humanize()})" else right.humanize())
		is HumanizedExpression -> toString()
		is InvalidExpression -> toString()
		else -> toString()
	}
}
*/

fun observableXExpression(prop: ObservableValue<Expression>, prefix:String="", suffix:String="") =
		prop.stringBinding { "$prefix$it$suffix" }
fun simpleStringBinding(prop: ObservableValue<String>, prefix:String="", suffix:String="") =
		prop.stringBinding { "$prefix$it$suffix" }
