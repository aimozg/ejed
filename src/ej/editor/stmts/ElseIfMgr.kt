package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlElseIf
import tornadofx.*

object ElseIfMgr : StatementManager<XlElseIf>() {
	override fun editorBody(stmt: XlElseIf) = StmtEditorBody(stmt) {
		label("Else, if condition ")
		textfield(stmt.testProperty)
		label(" is true")
		// TODO links to else, elseif
	}
	
	override fun treeGraphic(stmt: XlElseIf, tree: StatementTree) =
			StmtEditorLabel(stmt) {
				label(
						stmt.testProperty.stringBinding { "Else If: $it" }
				).addClass(Styles.xlogic)
			}
	
}

