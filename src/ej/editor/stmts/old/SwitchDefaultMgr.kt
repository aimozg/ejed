package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.XlSwitchDefault
import tornadofx.*

object SwitchDefaultMgr : StatementManager<XlSwitchDefault>() {
	override fun editorBody(stmt: XlSwitchDefault,
	                        tree: StatementTree
	) = defaultEditorBody {
		label("Default branch")
	}
	
	override fun treeGraphic(stmt: XlSwitchDefault, tree: StatementTree) =
			simpleTreeLabel("Default branch:").addClass(Styles.xlogic)
	
}