package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.binding3
import ej.editor.views.StatementTree
import ej.mod.XsButton
import tornadofx.*

object ButtonMgr : StatementManager<XsButton>() {
	override fun editorBody(stmt: XsButton) = defaultEditorBody {
		label("Offer choice")
		textfield(stmt.textProperty)
		label("leading to scene")
		textfield(stmt.refProperty)
		label(".")
		checkbox("Disabled", stmt.disabledProperty)
	}
	
	override fun treeGraphic(stmt: XsButton, tree: StatementTree) = simpleTreeLabel(
			binding3(stmt.textProperty,
			                         stmt.refProperty,
			                         stmt.disabledProperty) { text, ref, disabled ->
				(if (disabled == true) "(disabled) " else "") + "[$text] --> $ref"
			}
	).addClass(Styles.xlogic)
}