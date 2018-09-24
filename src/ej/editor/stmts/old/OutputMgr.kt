package ej.editor.stmts.old

import ej.editor.Styles
import ej.mod.XsOutput
import ej.utils.squeezeWs
import javafx.scene.layout.Priority
import tornadofx.*

object OutputMgr : StatementManager<XsOutput>() {
	override fun editorBody(stmt: XsOutput,
	                        tree: StatementTree
	) = defaultEditorBody() {
		label("Evaluate and display:")
		textfield(stmt.expression) { hgrow = Priority.ALWAYS }
	}
	
	override fun treeGraphic(stmt: XsOutput, tree: StatementTree) =
			simpleTreeLabel(stmt.expressionProperty.stringBinding {
				"Output: ${it?.squeezeWs()}"
			}).addClass(Styles.xcommand)
}