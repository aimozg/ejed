package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XsDisplay
import javafx.scene.control.Label
import tornadofx.*

object DisplayMgr : StatementManager<XsDisplay>() {
	override fun treeGraphic(stmt: XsDisplay, tree: StatementTree) =
			Label("Display: ${stmt.ref}").addClass(Styles.xcommand)
	
	override fun editorBody(stmt: XsDisplay) = StmtEditorBody<XsDisplay> {
		label("Display subscene: ")
		textfield(stmt.ref)
	}
	
}