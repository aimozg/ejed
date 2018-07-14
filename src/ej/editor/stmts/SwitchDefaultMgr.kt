package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlSwitchDefault
import tornadofx.*

object SwitchDefaultMgr : StatementManager<XlSwitchDefault>() {
	override fun editorBody(stmt: XlSwitchDefault) = defaultEditorBody {
		label("Default branch")
	}
	
	override fun treeGraphic(stmt: XlSwitchDefault, tree: StatementTree) =
			simpleTreeLabel("Default branch:").addClass(Styles.xlogic)
	
}