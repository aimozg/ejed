package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XsDisplay
import tornadofx.*

object DisplayMgr : StatementManager<XsDisplay>() {
	override fun treeGraphic(stmt: XsDisplay, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding{"Display: $it"}
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsDisplay) = defaultEditorBody {
		label("Display subscene: ")
		textfield(stmt.refProperty)
	}
	
}