package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.Builtins
import ej.mod.XComplexStatement
import ej.mod.XcScene
import ej.mod.XsForward
import javafx.scene.text.TextFlow
import tornadofx.*

object ForwardMgr : StatementManager<XsForward>() {
	override fun treeGraphic(stmt: XsForward, tree: StatementTree) =
			simpleTreeLabel(
					stmt.refProperty.stringBinding{"Forward => $it"}
			) {
				addClass(Styles.xcommand)
			}
	
	override fun editorBody(stmt: XsForward, rootStmt: XComplexStatement) = defaultEditorBody(TextFlow()) {
		text("Forward to scene: ")
		val mod = controller.mod ?: return@defaultEditorBody
		valueLink("Scene",
		          stmt.refProperty,
		          SceneChooser(rootStmt, mod, Builtins.scenes) { it is XcScene })
	}
	
}