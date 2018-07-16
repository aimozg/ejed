package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.editor.utils.bindingN
import ej.editor.views.StatementTree
import ej.mod.Builtins
import ej.mod.XComplexStatement
import ej.mod.XsButton
import ej.mod.acceptsMenu
import javafx.scene.text.TextFlow
import tornadofx.*

object ButtonMgr : StatementManager<XsButton>() {
	override fun editorBody(stmt: XsButton, rootStmt: XComplexStatement) = defaultEditorBody(TextFlow()) {
		text("Offer choice ")
		textfield(stmt.textProperty)
		text(" leading to scene ")
		valueLink("Scene",
		          stmt.refProperty,
		          SceneChooser(rootStmt,
		                       controller.mod ?: return@defaultEditorBody,
		                       Builtins.scenes) { it.acceptsMenu })
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