package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.stmts.defaultEditorBody
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.layout.Pane
import tornadofx.*

class ConstBool() : ExpressionBuilder() {
	val constant = SimpleBooleanProperty()
	
	constructor(value:Boolean):this() {
		constant.value = value
	}
	
	override fun text() = mktext(constant)
	
	override fun build(): Expression = when(constant.value) {
		true -> Identifier.True
		false -> Identifier.False
		null -> nop()
	}
	
	override fun editorBody(): Pane = defaultEditorBody {
		checkbox(null, constant)
	}
	
	override fun name(): String = "Always true / Always false"
	
	override fun copyMe() = ConstBool(constant.value)
	
	companion object : PartialBuilderConverter<Identifier> {
		override fun tryConvert(converter: BuilderConverter, expr: Identifier): ExpressionBuilder? = when (expr.value) {
			"true" -> ConstBool(true)
			"false" -> ConstBool(false)
			else -> null
		}
		
	}
}