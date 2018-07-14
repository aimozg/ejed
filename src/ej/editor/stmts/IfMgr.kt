package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.BoolExprChooser
import ej.editor.expr.simpleStringBinding
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
		button("...") {
			action {
				BoolExprChooser.pickValue()?.let { v ->
					stmt.test = v.build().source
				}
			}
		}
		// TODO else, elseif
	}
	
	override fun treeGraphic(stmt: XlIf, tree: StatementTree) =
			simpleTreeLabel(
					simpleStringBinding(stmt.testProperty, "If ")
				).addClass(Styles.xlogic)
	
}

