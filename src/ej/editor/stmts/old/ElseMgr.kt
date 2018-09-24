package ej.editor.stmts.old

import ej.editor.Styles
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
