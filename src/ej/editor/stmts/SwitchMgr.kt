package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.stringValueToggler
import ej.editor.views.StatementTree
import ej.mod.XlSwitch
import ej.mod.XlSwitchCase
import ej.mod.XlSwitchDefault
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

object SwitchMgr: StatementManager<XlSwitch>() {
	override fun editorBody(stmt: XlSwitch,
	                        tree: StatementTree
	) = defaultEditorBody(VBox()) {
		hbox(5.0) {
			label("Choose one of options")
			checkbox("using selector", stringValueToggler(stmt.valueProperty, "rand(100)"))
			textfield(stmt.valueProperty) {
				disableWhen { stmt.valueProperty.isNullOrEmpty() }
			}
		}
		hbox(5.0) {
			button("Add Branch") {
				action {
					val branch = XlSwitchCase()
					stmt.branches.add(branch)
					tree.focusOnStatement(branch)
				}
			}
			button("Add Default Branch") {
				disableWhen(stmt.defaultBranchProperty.isNotNull)
				action {
					val branch = XlSwitchDefault()
					stmt.defaultBranch = branch
					tree.focusOnStatement(branch)
				}
			}
		}
	}
	
	override fun treeGraphic(stmt: XlSwitch, tree: StatementTree) = simpleTreeLabel(
			stmt.valueProperty.stringBinding {
				if (it.isNullOrEmpty()) "Choose option"
				else "Choose using selector $it"
			}
	).addClass(Styles.xlogic)
	
}