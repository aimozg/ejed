package ej.editor.stmts

import ej.editor.Styles
import ej.mod.XlComment
import javafx.scene.layout.Priority
import tornadofx.*

class ScComment(stmt: XlComment) : StatementControl<XlComment>(stmt) {
	override fun createDefaultSkin() = ScSkin(this) {
		addClass(Styles.xcomment)
		textfield(stmt.textProperty) {
			hgrow = Priority.ALWAYS
		}
	}
}