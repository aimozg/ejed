package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.utils.NullableDoubleStringConverter
import ej.editor.utils.withPropertiesFrom
import javafx.scene.layout.Pane
import tornadofx.*

class ConstFloat : ConstBuilder<Double>() {
	override fun copyMe() = ConstFloat().withPropertiesFrom(this, ConstFloat::constant)
	
	override fun name() = "Decimal number"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		textfield(constant, NullableDoubleStringConverter)
	}
	
	override fun build() = constant.value?.let { FloatLiteral(it) } ?: nop()
	
	companion object : PartialBuilderConverter<FloatLiteral> {
		override fun tryConvert(converter: BuilderConverter, expr: FloatLiteral) =
				ConstFloat().apply {
					constant.value = expr.value
				}
		
	}
}
