package ej.editor.expr

import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane

class ConstInt : ExpressionBuilder() {
	override fun name() = "Integer number"
	
	override fun editorBody(): Pane = defaultBuilderBody {}
	override fun text() = mktext(constant)
	override fun build() = IntLiteral(constant.value)
	
	val constant = SimpleObjectProperty<Int>()
}