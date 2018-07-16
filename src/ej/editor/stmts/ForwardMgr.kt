package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XsForward
import tornadofx.*

object ForwardMgr : StatementManager<XsForward>() {
	override fun treeGraphic(stmt: XsForward, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding{"Forward => $it"}
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsForward) = defaultEditorBody {
		label("Forward to scene: ")
		textfield(stmt.refProperty)
	}
	
}