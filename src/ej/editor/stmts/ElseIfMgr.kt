package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.expressionBuilderStringBinding
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.views.StatementTree
import ej.mod.XlElseIf
import javafx.scene.layout.Priority
import tornadofx.*

object ElseIfMgr : StatementManager<XlElseIf>() {
	override fun editorBody(stmt: XlElseIf) = defaultEditorBody{
		label("Else if condition ")
		textfield(stmt.testProperty) { hgrow = Priority.SOMETIMES }
		label(" is true")
		button("...") {
			action {
				BoolExprChooser.pickValue("Condition", stmt.testProperty.toBuilder())?.let { v ->
					stmt.testProperty.fromBuilder(v)
				}
			}
		}
		// TODO goto else, elseif
	}
	
	override fun treeGraphic(stmt: XlElseIf, tree: StatementTree) =
			simpleTreeLabel(
					expressionBuilderStringBinding(stmt.testProperty, "Else If ")
			).addClass(Styles.xlogic)
	
}

