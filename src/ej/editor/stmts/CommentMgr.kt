package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlComment
import javafx.scene.layout.VBox
import tornadofx.*

object CommentMgr : StatementManager<XlComment>() {
	override fun treeGraphic(stmt: XlComment, tree: StatementTree) =
			simpleTreeLabel(
					stmt.textProperty.stringBinding { "// $it" }
			) {
				addClass(Styles.xcomment)
			}
	
	override fun editorBody(stmt: XlComment) = defaultEditorBody(VBox()) {
		label("Comments do nothing in game.")
		textarea(stmt.textProperty)
	}
	
}