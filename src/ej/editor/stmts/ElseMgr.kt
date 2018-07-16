package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XComplexStatement
import ej.mod.XlElse
import tornadofx.*

object ElseMgr : StatementManager<XlElse>() {
	override fun editorBody(stmt: XlElse, rootStmt: XComplexStatement) = defaultEditorBody {
		label("Else branch")
	}
	
	override fun treeGraphic(stmt: XlElse, tree: StatementTree) =
			simpleTreeLabel("Else:").addClass(Styles.xlogic)
	
}
