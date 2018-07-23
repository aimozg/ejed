package ej.editor.expr.external

import ej.editor.expr.Expression
import ej.editor.expr.ExpressionBuilder
import ej.editor.expr.nop
import ej.editor.stmts.defaultEditorBody
import javafx.beans.property.SimpleObjectProperty
import tornadofx.*

class ExternalEnumBuilder(val enumDecl: EnumDecl): ExpressionBuilder() {
	
	val valueProperty = SimpleObjectProperty<EnumDecl.EnumConstDecl?>()
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
	
	override fun copyMe(): ExpressionBuilder = ExternalEnumBuilder(enumDecl).also{other->
		other.value = this.value
	}
	
}