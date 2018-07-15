package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.utils.NullableIntStringConverter
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class ConstInt : ExpressionBuilder() {
	override fun name() = "Integer number"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		textfield(constant,NullableIntStringConverter)
	}
	override fun text() = mktext(constant)
	override fun build() = IntLiteral(constant.value)
	
	val constant = SimpleObjectProperty<Int>()
	
	companion object : PartialBuilderConverter<IntLiteral> {
		override fun tryConvert(converter: BuilderConverter, expr: IntLiteral) =
				ConstInt().apply {
					constant.value = expr.value
				}
		
	}
}