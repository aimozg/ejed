package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.XsDisplay
import ej.mod.acceptsMenu
import tornadofx.*

object DisplayMgr : StatementManager<XsDisplay>() {
	override fun treeGraphic(stmt: XsDisplay, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding { "Display: $it" }
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsDisplay,
	                        tree: StatementTree
	) = defaultEditorTextFlow {
		text("Display named text: ")
		valueLink(stmt.refProperty,
		          "Named text",
		          SceneChooser(tree.rootStatement, controller.mod ?: return@defaultEditorTextFlow) { !it.acceptsMenu })
	}
	
}