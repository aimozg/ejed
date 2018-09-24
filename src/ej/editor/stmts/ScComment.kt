package ej.editor.stmts

import ej.editor.Styles
import ej.mod.XlComment
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class ScComment(stmt: XlComment) : StatementControl<XlComment>(stmt) {
	override fun createDefaultSkin() = CommentSkin()
	inner class CommentSkin : ScSkin<XlComment, ScComment>(this) {
		override fun VBox.body() {
			addClass(Styles.xcomment)
			textfield(stmt.textProperty) {
				hgrow = Priority.ALWAYS
			}
		}
	}
}