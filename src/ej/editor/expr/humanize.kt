package ej.editor.expr

import javafx.beans.value.ObservableValue
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

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
	}
}

fun observableXExpression(prop: ObservableValue<String>, prefix:String="", suffix:String="") =
		prop.stringBinding {
			try {
				prefix + parseExpression(it?:"").humanize() + suffix
			} catch (e:Throwable) {
				"<ERROR IN EXPRESSION: ${e.message}"
			}
		}
