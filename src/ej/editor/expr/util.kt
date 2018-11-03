package ej.editor.expr

import javafx.beans.value.ObservableValue
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

val Expression.asId get() = (this as? Identifier)
val Expression.asDot get() = (this as? DotExpression)
val Expression.asStringLiteral get() = (this as? StringLiteral)


fun simpleStringBinding(prop: ObservableValue<String>, prefix:String="", suffix:String="") =
		prop.stringBinding { "$prefix$it$suffix" }
fun expressionBuilderStringBinding(prop: ExpressionProperty, prefix:String="", suffix:String="") =
		prop.stringBinding { "$prefix${prop.toBuilder().text()}$suffix" }

fun nop() = InvalidExpression("")