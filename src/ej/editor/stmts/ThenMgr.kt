package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlThen
import tornadofx.*

object ThenMgr : StatementManager<XlThen>() {
	override fun editorBody(stmt: XlThen,
	                        tree: StatementTree
	) = defaultEditorBody {
		label("Then")
		// TODO else, elseif
	}
	
	override fun treeGraphic(stmt: XlThen, tree: StatementTree) =
			simpleTreeLabel("Then:").addClass(Styles.xlogic)
	
}