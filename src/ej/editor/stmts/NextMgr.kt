package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XComplexStatement
import ej.mod.XsNext
import tornadofx.*

object NextMgr : StatementManager<XsNext>() {
	override fun editorBody(stmt: XsNext, rootStmt: XComplexStatement) = defaultEditorBody {
		label("Proceed to scene")
		textfield(stmt.refProperty)
	}

	override fun treeGraphic(stmt: XsNext, tree: StatementTree) = simpleTreeLabel(
			stmt.refProperty.stringBinding { "[Next] --> $it" }
	).addClass(Styles.xlogic)
}