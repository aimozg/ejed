package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.utils.bindingN
import ej.editor.utils.observableUnique
import ej.editor.utils.presentWhen
import ej.mod.XStatement
import ej.mod.XlIf
import tornadofx.*

/*
 * Created by aimozg on 22.09.2018.
 * Confidential until published on GitHub
 */
class ScIf(stmt: XlIf) : StatementControl<XlIf>(stmt) {
	override fun createDefaultSkin() = IfSkin()
	
	inner class IfSkin : ScSkin<XlIf, ScIf>(this, {
		addClass(Styles.xlogic)
		stmtList(stmt.thenGroup.content) {
			beforeList = hbox {
				children += detachListMenu()
				scFlow(Styles.xlogic) {
					text("If condition ")
					valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter = {
						if (it != null) stmt.testProperty.fromBuilder(it)
					})
					text(" is true")
				}
			}
		}
		simpleList(stmt.elseifGroups) { elseif ->
			stmtList(elseif.content) {
				beforeList = hbox {
					children += detachListMenu()
					scFlow(Styles.xlogic) {
						text("Else if condition ")
						valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter = {
							if (it != null) stmt.testProperty.fromBuilder(it)
						})
						text(" is true:")
					}
				}
			}
		}
		stmtList(bindingN(stmt.elseGroupProperty) {
			it?.content ?: emptyList<XStatement>().observableUnique()
		}) {
			beforeList = hbox {
				children += detachListMenu()
				scFlow(Styles.xlogic) {
					text("Else:") {
						addClass(Styles.xlogic)
					}
					presentWhen(stmt.elseGroupProperty.isNotNull)
				}
			}
			presentWhen(stmt.elseGroupProperty.isNotNull)
		}
	})
}

