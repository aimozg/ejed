package ej.editor.stmts.old

import ej.editor.Styles
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