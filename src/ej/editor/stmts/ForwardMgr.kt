package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.Builtins
import ej.mod.XsForward
import ej.mod.acceptsMenu
import tornadofx.*

object ForwardMgr : StatementManager<XsForward>() {
	override fun treeGraphic(stmt: XsForward, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding{"Forward => $it"}
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsForward,
	                        tree: StatementTree
	) = defaultEditorTextFlow {
		text("Forward to scene: ")
		valueLink(stmt.refProperty,
		          "Scene",
		          SceneChooser(tree.rootStatement,
		                       controller.mod ?: return@defaultEditorTextFlow,
		                       Builtins.scenes) { it.acceptsMenu })
	}
	
}