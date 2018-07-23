package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.Natives
import ej.mod.XsNext
import ej.mod.acceptsMenu
import tornadofx.*

object NextMgr : StatementManager<XsNext>() {
	override fun editorBody(stmt: XsNext,
	                        tree: StatementTree
	) = defaultEditorTextFlow {
		text("Proceed to scene ")
		valueLink(stmt.refProperty,
		          "Scene",
		          SceneChooser(tree.rootStatement,
		                       controller.mod ?: return@defaultEditorTextFlow,
		                       Natives.scenes.map { it.ref }) { it.acceptsMenu })
	}

	override fun treeGraphic(stmt: XsNext, tree: StatementTree) = simpleTreeLabel(
			stmt.refProperty.stringBinding { "[Next] --> $it" }
	).addClass(Styles.xnext)
}