package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.Builtins
import ej.mod.XsNext
import ej.mod.acceptsMenu
import javafx.scene.text.TextFlow
import tornadofx.*

object NextMgr : StatementManager<XsNext>() {
	override fun editorBody(stmt: XsNext,
	                        tree: StatementTree
	) = defaultEditorBody(TextFlow()) {
		text("Proceed to scene ")
		valueLink(stmt.refProperty,
		          "Scene",
		          SceneChooser(tree.rootStatement,
		                       controller.mod ?: return@defaultEditorBody,
		                       Builtins.scenes) { it.acceptsMenu })
	}

	override fun treeGraphic(stmt: XsNext, tree: StatementTree) = simpleTreeLabel(
			stmt.refProperty.stringBinding { "[Next] --> $it" }
	).addClass(Styles.xlogic)
}