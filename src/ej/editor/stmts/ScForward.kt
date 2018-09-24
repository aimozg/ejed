package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsForward
import ej.mod.acceptsMenu
import javafx.scene.layout.VBox
import tornadofx.*

class ScForward(stmt: XsForward) : StatementControl<XsForward>(stmt) {
	override fun createDefaultSkin() = ForwardSkin()
	
	inner class ForwardSkin : ScSkin<XsForward, ScForward>(this) {
		override fun VBox.body() {
			scFlow(Styles.xnext) {
				text("Forward to ")
				valueLink(stmt.refProperty,
				          "Scene",
				          SceneChooser(rootStatement() ?: return@scFlow,
				                       mod() ?: return@scFlow) { !it.acceptsMenu })
			}
		}
		
	}
}