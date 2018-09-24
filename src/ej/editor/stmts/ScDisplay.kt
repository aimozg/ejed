package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsDisplay
import ej.mod.acceptsMenu
import javafx.scene.layout.VBox
import tornadofx.*

class ScDisplay(stmt: XsDisplay) : StatementControl<XsDisplay>(stmt) {
	override fun createDefaultSkin() = DisplaySkin()
	
	inner class DisplaySkin : ScSkin<XsDisplay, ScDisplay>(this) {
		override fun VBox.body() {
			scFlow(Styles.xcommand) {
				text("Display ")
				valueLink(stmt.refProperty,
				          "Named text",
				          SceneChooser(rootStatement() ?: return@scFlow,
				                       mod() ?: return@scFlow) { !it.acceptsMenu })
			}
		}
		
	}
}