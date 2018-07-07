package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XsDisplay
import tornadofx.*

object DisplayMgr : StatementManager<XsDisplay>() {
	override fun treeGraphic(stmt: XsDisplay, tree: StatementTree) =
			StmtEditorLabel(stmt) {
				label("Display: ${stmt.ref}").addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsDisplay) = StmtEditorBody(stmt) {
		label("Display subscene: ")
		textfield(stmt.ref)
	}
	
}