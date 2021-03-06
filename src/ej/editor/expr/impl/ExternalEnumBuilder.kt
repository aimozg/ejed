package ej.editor.expr.impl

import ej.editor.expr.Expression
import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.nop
import ej.editor.external.EnumDecl
import ej.editor.stmts.old.defaultEditorBody
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class ExternalEnumBuilder(val enumDecl: EnumDecl, initial: EnumDecl.EnumConstDecl? = null) : ExpressionBuilder() {
	init {
		if (initial != null && initial !in enumDecl.values) kotlin.error("$initial not in $enumDecl")
	}
	
	val valueProperty = SimpleObjectProperty(initial)
	var value by valueProperty
	
	override fun text() = value?.text()?:"<???>"
	
	override fun build(): Expression = value?.implExpr?: nop()
	
	override fun editorBody() = defaultEditorBody {
		listview(enumDecl.values) {
			cellFormat {
				text = item.name
			}
			selectionModel.select(value)
			valueProperty.cleanBind(selectionModel.selectedItemProperty())
		}
	}
	
	override fun name(): String = "Constant value "+enumDecl.name
	
	override fun copyMe(): ExpressionBuilder = ExternalEnumBuilder(enumDecl).also { other ->
		other.value = this.value
	}
	
	override fun initializableBy(initial: ExpressionBuilder): Boolean {
		return (initial as? ExternalEnumBuilder)?.enumDecl == enumDecl
	}
}