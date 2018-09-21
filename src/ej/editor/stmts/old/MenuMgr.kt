package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.XsMenu
import tornadofx.*

object MenuMgr : StatementManager<XsMenu>() {
	override fun editorBody(stmt: XsMenu,
	                        tree: StatementTree
	) = defaultEditorBody {
		label("Menu")
		// TODO goto choices
	}
	
	override fun treeGraphic(stmt: XsMenu, tree: StatementTree) = simpleTreeLabel("Menu:").addClass(Styles.xnext)
	
}

