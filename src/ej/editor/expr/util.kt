package ej.editor.expr

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

val Expression.asId get() = (this as? Identifier)
val Expression.asDot get() = (this as? DotExpression)