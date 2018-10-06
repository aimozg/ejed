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
		val ifNode = group {
			scFlow(Styles.xlogic) {
				layoutX = 32.0
				text("If condition ")
				valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter = {
					if (it != null) stmt.testProperty.fromBuilder(it)
				})
				text(" is true")
			}
		}
		val thenList = stmtList(stmt.thenGroup.content)
		ifNode.children.add(0, thenList.detachListMenu())
		simpleList(stmt.elseifGroups) { elseif ->
			val elseNode = group {
				scFlow(Styles.xlogic) {
					layoutX = 32.0
					text("Else if condition ")
					valueLink("Condition", stmt.testProperty.toBuilder(), BoolExprChooser, setter = {
						if (it != null) stmt.testProperty.fromBuilder(it)
					})
					text(" is true:")
				}
			}
			val elseList = stmtList(elseif.content)
			elseNode.children.add(0, elseList.detachListMenu())
		}
		val elseNode = group {
			scFlow(Styles.xlogic) {
				layoutX = 32.0
				text("Else:") {
					addClass(Styles.xlogic)
				}
				presentWhen(stmt.elseGroupProperty.isNotNull)
			}
		}
		val elseList = stmtList(bindingN(stmt.elseGroupProperty) {
			it?.content ?: emptyList<XStatement>().observableUnique()
		}) {
			presentWhen(stmt.elseGroupProperty.isNotNull)
		}
		elseNode.children.add(0, elseList.detachListMenu())
	})
}

