package ej.editor.stmts

import ej.editor.Styles
import ej.mod.XsMenu
import tornadofx.*

class ScMenu(stmt: XsMenu) : StatementControl<XsMenu>(stmt) {
	override fun createDefaultSkin() = MenuSkin()
	inner class MenuSkin : ScSkin<XsMenu, ScMenu>(this, {
		addClass(Styles.xcommand)
		stmtList(stmt.content) {
			beforeList = hbox {
				children += listTopMenu
				scFlow(Styles.xcommand) {
					text("Menu:")
				}
			}
		}
	})
}