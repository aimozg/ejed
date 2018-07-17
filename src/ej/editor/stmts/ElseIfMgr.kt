package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.expressionBuilderStringBinding
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.XlElseIf
import javafx.scene.layout.VBox
import tornadofx.*

object ElseIfMgr : StatementManager<XlElseIf>() {
	override fun editorBody(stmt: XlElseIf,
	                        tree: StatementTree
	) = defaultEditorBody(VBox()){
		textflow {
			text("Else if condition ")
			valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter={
				if (it != null) stmt.testProperty.fromBuilder(it)
			})
			text(" is true")
		}
		/* TODO edit source
		button("...") */
	}
	
	override fun treeGraphic(stmt: XlElseIf, tree: StatementTree) =
			simpleTreeLabel(
					expressionBuilderStringBinding(stmt.testProperty, "Else If ")
			).addClass(Styles.xlogic)
	
}

