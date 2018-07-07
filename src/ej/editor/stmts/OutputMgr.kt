package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XsOutput
import ej.utils.squeezeWs
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import tornadofx.*

object OutputMgr : StatementManager<XsOutput>() {
	override fun editorBody(stmt: XsOutput) = StmtEditorBody<XsOutput> {
		label("Evaluate and display:")
		textfield(stmt.expression) { hgrow = Priority.ALWAYS }
	}
	
	override fun treeGraphic(stmt: XsOutput, tree: StatementTree) =
			Label("Output: ${stmt.expression.squeezeWs()}").addClass(Styles.xcommand)
}