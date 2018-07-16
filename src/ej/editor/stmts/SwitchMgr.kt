package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.stringValueToggler
import ej.editor.views.StatementTree
import ej.mod.XComplexStatement
import ej.mod.XlSwitch
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

object SwitchMgr: StatementManager<XlSwitch>() {
	override fun editorBody(stmt: XlSwitch, rootStmt: XComplexStatement) = defaultEditorBody {
		label("Choose one of options")
		checkbox("using selector",stringValueToggler(stmt.valueProperty,"rand(100)"))
		textfield(stmt.valueProperty) {
			disableWhen { stmt.valueProperty.isNullOrEmpty() }
		}
	}
	
	override fun treeGraphic(stmt: XlSwitch, tree: StatementTree) = simpleTreeLabel(
			stmt.valueProperty.stringBinding {
				if (it.isNullOrEmpty()) "Choose option"
				else "Choose using selector $it"
			}
	).addClass(Styles.xlogic)
	
}