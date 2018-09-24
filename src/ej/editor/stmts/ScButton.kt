package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.Natives
import ej.mod.XsButton
import ej.mod.acceptsMenu
import javafx.scene.layout.VBox
import tornadofx.*

class ScButton(stmt: XsButton) : StatementControl<XsButton>(stmt) {
	override fun createDefaultSkin() = ButtonSkin()
	inner class ButtonSkin : ScSkin<XsButton, ScButton>(this) {
		override fun VBox.body() {
			addClass(Styles.xnext)
			scFlow(Styles.xnext) {
				text("Choice ")
				textfield(stmt.textProperty)
				text(" --> ")
				valueLink(stmt.refProperty,
				          "Scene",
				          SceneChooser(rootStatement() ?: return@scFlow,
				                       mod() ?: return@scFlow,
				                       Natives.scenes.map { it.ref }) { it.acceptsMenu })
				text(" ")
				checkbox("Disabled", stmt.disabledProperty)
			}
		}
	}
}