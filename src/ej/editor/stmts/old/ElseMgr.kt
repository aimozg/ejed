package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.XlElse
import tornadofx.*

object ElseMgr : StatementManager<XlElse>() {
	override fun editorBody(stmt: XlElse,
	                        tree: StatementTree
	) = defaultEditorBody {
		label("Else branch")
	}
	
	override fun treeGraphic(stmt: XlElse, tree: StatementTree) =
			simpleTreeLabel("Else:").addClass(Styles.xlogic)
	
}
