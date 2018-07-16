package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.XComplexStatement
import ej.mod.XsDisplay
import ej.mod.acceptsMenu
import javafx.scene.text.TextFlow
import tornadofx.*

object DisplayMgr : StatementManager<XsDisplay>() {
	override fun treeGraphic(stmt: XsDisplay, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding { "Display: $it" }
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsDisplay, rootStmt: XComplexStatement) = defaultEditorBody(TextFlow()) {
		text("Display subscene: ")
		valueLink("Subscene",
		          stmt.refProperty,
		          SceneChooser(rootStmt, controller.mod ?: return@defaultEditorBody) { !it.acceptsMenu })
	}
	
}