package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.simpleStringBinding
import ej.editor.views.StatementTree
import ej.mod.XlElseIf
import javafx.scene.layout.Priority
import tornadofx.*

object ElseIfMgr : StatementManager<XlElseIf>() {
	override fun editorBody(stmt: XlElseIf) = defaultEditorBody{
		label("Else if condition ")
		textfield(stmt.testProperty) { hgrow = Priority.SOMETIMES }
		label(" is true")
		// TODO else, elseif
	}
	
	override fun treeGraphic(stmt: XlElseIf, tree: StatementTree) =
			simpleTreeLabel(
					simpleStringBinding(stmt.testProperty, "Else If ")
			).addClass(Styles.xlogic)
	
}

