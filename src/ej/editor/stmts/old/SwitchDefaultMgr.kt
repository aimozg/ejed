package ej.editor.stmts.old

import ej.editor.Styles
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