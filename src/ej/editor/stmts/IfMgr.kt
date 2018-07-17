package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.expressionBuilderStringBinding
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.XlElse
import ej.mod.XlElseIf
import ej.mod.XlIf
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */
object IfMgr : StatementManager<XlIf>() {
	override fun editorBody(stmt: XlIf, tree: StatementTree) = defaultEditorBody(VBox()) {
		textflow {
			text("If condition ")
			valueLink("Condition",stmt.testProperty.toBuilder(),BoolExprChooser,setter={
				if (it != null) stmt.testProperty.fromBuilder(it)
			})
			text(" is true")
			/*
			TODO edit source
			button("...")
			*/
		}
		hbox(5.0) {
			button("Add ElseIf") {
				action {
					val elseIf = XlElseIf()
					stmt.elseifGroups.add(elseIf)
					tree.focusOnStatement(elseIf)
				}
			}
			button("Add Else") {
				disableWhen(stmt.elseGroupProperty.isNotNull)
				action {
					val elseGroup = XlElse()
					stmt.elseGroup = elseGroup
					tree.focusOnStatement(elseGroup)
				}
			}
		}
	}
	
	override fun treeGraphic(stmt: XlIf, tree: StatementTree) =
			simpleTreeLabel(
					expressionBuilderStringBinding(stmt.testProperty, "If ")
				).addClass(Styles.xlogic)
	
}

