package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.XlThen
import tornadofx.*

object ThenMgr : StatementManager<XlThen>() {
	override fun editorBody(stmt: XlThen,
	                        tree: StatementTree
	) = defaultEditorBody {
		label("Then")
	}
	
	override fun treeGraphic(stmt: XlThen, tree: StatementTree) =
			simpleTreeLabel("Then:").addClass(Styles.xlogic)
	
}