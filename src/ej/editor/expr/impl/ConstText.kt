package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.utils.withPropertiesFrom
import javafx.scene.layout.Pane
import tornadofx.*

class ConstText : ConstBuilder<String>() {
	override fun copyMe() = ConstText().withPropertiesFrom(this, ConstText::constant)
	
	override fun name() = "Text string"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		textfield {
			textProperty().bindBidirectional(constant)
		}
	}
	
	override fun build() = constant.value?.let { StringLiteral(it) } ?: nop()
	
	companion object : PartialBuilderConverter<StringLiteral> {
		override fun tryConvert(converter: BuilderConverter, expr: StringLiteral) =
				ConstText().apply {
					constant.value = expr.value
				}
		
	}
}