package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.utils.NullableIntStringConverter
import ej.editor.utils.withPropertiesFrom
import javafx.scene.layout.Pane
import tornadofx.*

class ConstInt : ConstBuilder<Int>() {
	override fun copyMe() = ConstInt().withPropertiesFrom(this,ConstInt::constant)
	
	override fun name() = "Integer number"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		textfield(constant,NullableIntStringConverter)
	}
	override fun build() = constant.value?.let { IntLiteral(it) }?: nop()
	
	companion object : PartialBuilderConverter<IntLiteral> {
		override fun tryConvert(converter: BuilderConverter, expr: IntLiteral) =
				ConstInt().apply {
					constant.value = expr.value
				}
		
	}
}