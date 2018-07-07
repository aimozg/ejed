package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlElse
import tornadofx.*

object ElseMgr : StatementManager<XlElse>() {
	override fun editorBody(stmt: XlElse) = StmtEditorBody(stmt) {
		label("Else")
	}
	
	override fun treeGraphic(stmt: XlElse, tree: StatementTree) =
			StmtEditorLabel(stmt) {
				label("Else:").addClass(Styles.xlogic)
			}
	
}