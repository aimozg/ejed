package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.utils.bindingN
import ej.editor.utils.observableUnique
import ej.editor.utils.presentWhen
import ej.mod.XStatement
import ej.mod.XlElse
import ej.mod.XlElseIf
import ej.mod.XlIf
import tornadofx.*

/*
 * Created by aimozg on 22.09.2018.
 * Confidential until published on GitHub
 */
class ScIf(stmt: XlIf) : StatementControl<XlIf>(stmt) {
	override fun createDefaultSkin() = IfSkin()
	
	inner class IfSkin : ScSkin<XlIf, ScIf>(this, {
		addClass("sc-if")
		mergedContextMenu().apply {
			item("Add Else-If") {
				action {
					stmt.elseifGroups.add(XlElseIf())
				}
			}
			item("Add Else") {
				action {
					stmt.elseGroup = XlElse()
				}
				enableWhen(stmt.elseGroupProperty.isNull)
			}
		}
		addClass(Styles.xlogic)
		stmtList(stmt.thenGroup.content) {
			addClass("sc-if-then")
			beforeList = hbox {
				children += detachListMenu()
				scFlow(Styles.xlogic) {
					text("If ")
					valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter = {
						if (it != null) stmt.testProperty.fromBuilder(it)
					})
				}
			}
		}
		simpleList(stmt.elseifGroups) { elseif ->
			stmtList(elseif.content) {
				addClass("sc-if-elseif")
				beforeList = hbox {
					children += detachListMenu()
					scFlow(Styles.xlogic) {
						text("Else if ")
						valueLink("Condition", elseif.testProperty.toBuilder(), BoolExprChooser, setter = {
							if (it != null) elseif.testProperty.fromBuilder(it)
						})
					}
				}
			}
		}.addClass("sc-if-elseifs")
		stmtList(bindingN(stmt.elseGroupProperty) {
			it?.content ?: emptyList<XStatement>().observableUnique()
		}) {
			addClass("sc-if-else")
			beforeList = hbox {
				children += detachListMenu()
				scFlow(Styles.xlogic) {
					text("Else") {
						addClass(Styles.xlogic)
					}
					presentWhen(stmt.elseGroupProperty.isNotNull)
				}
			}
			presentWhen(stmt.elseGroupProperty.isNotNull)
		}
	})
}

