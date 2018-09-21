package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.stmts.StatementManager
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.simpleTreeLabel
import ej.editor.utils.bindingN
import ej.editor.utils.colspan
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.stringValueToggler
import ej.editor.views.StatementTree
import ej.mod.XlSwitchCase
import ej.utils.affixNonEmpty
import javafx.scene.layout.GridPane
import tornadofx.*

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */
object SwitchCaseMgr : StatementManager<XlSwitchCase>() {
	override fun editorBody(stmt: XlSwitchCase,
	                        tree: StatementTree
	) = defaultEditorBody(GridPane()) {
		row {
			label("Branch when") {
				gridpaneConstraints { colspan(2) }
			}
		}
		row {
			checkbox("condition is true:", stringValueToggler(stmt.testProperty, "true"))
			textfield(stmt.testProperty) {
				disableWhen(stmt.testProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("selector = ", stringValueToggler(stmt.valueProperty, "0"))
			textfield(stmt.valueProperty) {
				disableWhen(stmt.valueProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("selector ≠ ", stringValueToggler(stmt.neProperty, "0"))
			textfield(stmt.neProperty) {
				disableWhen(stmt.neProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("selector > ", stringValueToggler(stmt.gtProperty, "50"))
			textfield(stmt.gtProperty) {
				disableWhen(stmt.gtProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("selector ≥ ", stringValueToggler(stmt.gteProperty, "50"))
			textfield(stmt.gteProperty) {
				disableWhen(stmt.gteProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("selector < ", stringValueToggler(stmt.ltProperty, "50"))
			textfield(stmt.ltProperty) {
				disableWhen(stmt.ltProperty.isNullOrEmpty())
			}
		}
		row {
			checkbox("select value ≤ ", stringValueToggler(stmt.lteProperty, "50"))
			textfield(stmt.lteProperty) {
				disableWhen(stmt.lteProperty.isNullOrEmpty())
			}
		}
	}
	
	override fun treeGraphic(stmt: XlSwitchCase, tree: StatementTree) = simpleTreeLabel(bindingN(
			stmt.testProperty,
			stmt.valueProperty, stmt.neProperty,
			stmt.gtProperty, stmt.gteProperty,
			stmt.ltProperty, stmt.lteProperty
	) { test, value, ne, gt, gte, lt, lte ->
		"Branch when " + listOf(
				test ?: "",
				value.affixNonEmpty("selector = "),
				ne.affixNonEmpty("selector ≠ "),
				gt.affixNonEmpty("selector > "),
				gte.affixNonEmpty("selector ≥ "),
				lt.affixNonEmpty("selector < "),
				lte.affixNonEmpty("selector ≤ ")
		).filter { it.isNotEmpty() }.joinToString("; and ")
	}).addClass(Styles.xlogic)
	
}