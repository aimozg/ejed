package ej.editor.expr.impl

import ej.editor.expr.Expression
import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.ExpressionProperty
import ej.editor.stmts.old.defaultEditorBody
import javafx.scene.layout.Priority
import tornadofx.*

class RawExpressionBuilder() : ExpressionBuilder() {
	constructor(expression: Expression): this() {
		this.source.expressionProperty.value = expression
	}
	constructor(source:String): this() {
		this.source.value = source
	}
	
	override fun copyMe() = RawExpressionBuilder(source.value)
	val source = ExpressionProperty("")
	override fun build(): Expression = source.expressionProperty.value
	
	override fun editorBody() = defaultEditorBody {
		textarea(source) {
			vgrow = Priority.ALWAYS
			hgrow = Priority.ALWAYS
		}
	}
	
	override fun text() = "`${source.value}`"
	
	override fun name() = "Code"
}