package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.utils.bindingN
import ej.editor.views.StatementTree
import ej.mod.Natives
import ej.mod.XsButton
import ej.mod.acceptsMenu
import tornadofx.*

object ButtonMgr : StatementManager<XsButton>() {
	override fun editorBody(stmt: XsButton,
	                        tree: StatementTree
	) = defaultEditorTextFlow  {
		text("Offer choice ")
		textfield(stmt.textProperty)
		text(" leading to scene ")
		valueLink(stmt.refProperty,
		          "Scene",
		          SceneChooser(tree.rootStatement,
		                       controller.mod ?: return@defaultEditorTextFlow,
		                       Natives.scenes.map { it.ref }) { it.acceptsMenu })
		text(". ")
		checkbox("Disabled", stmt.disabledProperty)
	}
	
	override fun treeGraphic(stmt: XsButton, tree: StatementTree) = simpleTreeLabel(
			bindingN(stmt.textProperty,
			         stmt.refProperty,
			         stmt.disabledProperty) { text, ref, disabled ->
				(if (disabled == true) "(disabled) " else "") + "[$text] --> $ref"
			}
	).addClass(Styles.xlogic)
}