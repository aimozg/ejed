package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.observableXExpression
import ej.editor.views.StatementTree
import ej.mod.XlIf
import javafx.scene.layout.Priority
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */
object IfMgr : StatementManager<XlIf>() {
	override fun editorBody(stmt: XlIf) = defaultEditorBody{
		label("If condition ")
		textfield(stmt.testProperty) { hgrow = Priority.SOMETIMES }
		label(" is true")
		// TODO else, elseif
	}
	
	override fun treeGraphic(stmt: XlIf, tree: StatementTree) =
			simpleTreeLabel(
					observableXExpression(stmt.testProperty, "If ")
				).addClass(Styles.xlogic)
	
}

