package ej.editor.stmts

import ej.editor.Styles
import ej.mod.XsOutput
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class ScOutput(stmt: XsOutput) : StatementControl<XsOutput>(stmt) {
	override fun createDefaultSkin() = OutputSkin()
	inner class OutputSkin : ScSkin<XsOutput, ScOutput>(this) {
		override fun VBox.body() {
			addClass(Styles.xcommand)
			label("Evaluate and display:")
			textfield(stmt.expression) { hgrow = Priority.ALWAYS }
		}
	}
}