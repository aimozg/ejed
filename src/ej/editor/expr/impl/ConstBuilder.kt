package ej.editor.expr.impl

import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.mktext
import javafx.beans.property.SimpleObjectProperty

abstract class ConstBuilder<T> : ExpressionBuilder() {
	val constant = SimpleObjectProperty<T?>()
	override fun text() = mktext(constant)
}