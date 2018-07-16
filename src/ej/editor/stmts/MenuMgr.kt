package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XComplexStatement
import ej.mod.XsMenu
import tornadofx.*

object MenuMgr : StatementManager<XsMenu>() {
	override fun editorBody(stmt: XsMenu, rootStmt: XComplexStatement) = defaultEditorBody {
		label("Menu")
		// TODO goto choices
	}
	
	override fun treeGraphic(stmt: XsMenu, tree: StatementTree) = simpleTreeLabel("Menu:").addClass(Styles.xlogic)
	
}

