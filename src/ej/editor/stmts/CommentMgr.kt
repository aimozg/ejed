package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlComment
import tornadofx.*

object CommentMgr : StatementManager<XlComment>() {
	override fun treeGraphic(stmt: XlComment, tree: StatementTree) =
			simpleTreeLabel(
					stmt.textProperty.stringBinding { "// $it" }
			) {
				addClass(Styles.xcomment)
			}
	
	override fun editorBody(stmt: XlComment) = vboxEditorBody {
		label("Comments do nothing in game.")
		textarea(stmt.textProperty)
	}
	
}