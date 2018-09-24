package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.autoStretch
import ej.mod.XlComment
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

class ScComment(stmt: XlComment) : StatementControl<XlComment>(stmt) {
	override fun createDefaultSkin() = CommentSkin()
	inner class CommentSkin : ScSkin<XlComment, ScComment>(this) {
		override fun VBox.body() {
			addClass(Styles.xcomment)
			textarea(stmt.textProperty) {
				isWrapText = true
				hgrow = Priority.ALWAYS
				autoStretch()
			}
		}
	}
}