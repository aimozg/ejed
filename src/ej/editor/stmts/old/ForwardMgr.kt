package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.stmts.SceneChooser
import ej.editor.stmts.StatementManager
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.Natives
import ej.mod.XsForward
import ej.mod.acceptsMenu
import tornadofx.*

object ForwardMgr : StatementManager<XsForward>() {
	override fun treeGraphic(stmt: XsForward, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding { "Forward => $it" }
			) {
				addClass(Styles.xnext)
			}
	
	override fun editorBody(stmt: XsForward,
	                        tree: StatementTree
	) = defaultEditorTextFlow {
		text("Forward to scene: ")
		valueLink(stmt.refProperty,
		          "Scene",
		          SceneChooser(tree.rootStatement,
		                                       controller.mod ?: return@defaultEditorTextFlow,
		                                       Natives.scenes.map { it.ref }) { it.acceptsMenu })
	}
	
}