package ej.editor.stmts

import ej.editor.Styles
import ej.mod.XsMenu
import javafx.scene.layout.VBox
import tornadofx.*

class ScMenu(stmt: XsMenu) : StatementControl<XsMenu>(stmt) {
	override fun createDefaultSkin() = MenuSkin()
	inner class MenuSkin : ScSkin<XsMenu, ScMenu>(this) {
		override fun VBox.body() {
			addClass(Styles.xcommand)
			text("Menu:")
			stmtList(stmt.content) {
				translateX = 12.0
			}
		}
	}
}