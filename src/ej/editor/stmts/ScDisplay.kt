package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsDisplay
import ej.mod.acceptsMenu
import tornadofx.*

class ScDisplay(stmt: XsDisplay) : StatementControl<XsDisplay>(stmt) {
	override fun createDefaultSkin() = ScSkin(this) {
		scFlow(Styles.xcommand) {
			text("Display ")
			valueLink(stmt.refProperty,
			          "Named text",
			          SceneChooser(rootStatement() ?: return@scFlow,
			                       mod() ?: return@scFlow) { !it.acceptsMenu })
		}
	}
}