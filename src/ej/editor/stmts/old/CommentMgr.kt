package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.views.StatementTree
import ej.mod.XlComment
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

object CommentMgr : StatementManager<XlComment>() {
	override fun treeGraphic(stmt: XlComment, tree: StatementTree) =
			simpleTreeLabel(
					stmt.textProperty.stringBinding { "// $it" }
			) {
				addClass(Styles.xcomment)
			}
	
	override fun editorBody(stmt: XlComment,
	                        tree: StatementTree
	) = defaultEditorBody(VBox()) {
		label("Comments do nothing in game.") {
			vgrow = Priority.NEVER
		}
		textarea(stmt.textProperty) {
			vgrow = Priority.ALWAYS
		}
	}
	
}