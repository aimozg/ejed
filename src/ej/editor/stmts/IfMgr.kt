package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XlIf
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */
object IfMgr : StatementManager<XlIf>() {
	override fun editorBody(stmt: XlIf) = StmtEditorBody(stmt) {
		label("If condition ")
		textfield(stmt.testProperty())
		label(" is true")
		// TODO else, elseif
	}
	
	override fun treeGraphic(stmt: XlIf, tree: StatementTree) =
			StmtEditorLabel(stmt) {
				label(
						stmt.testProperty().stringBinding { "If: $it" }
				).addClass(Styles.xlogic)
			}
	
}