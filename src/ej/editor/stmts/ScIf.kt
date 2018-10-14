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
import ej.utils.addAfter
import ej.utils.addBefore
import ej.utils.crop
import javafx.scene.input.KeyCombination
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
			item("Delete Else") {
				action {
					stmt.elseGroup = null
				}
				enableWhen(stmt.elseGroupProperty.isNotNull)
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
					contextmenu {
						item("Add Else-If Above", KeyCombination.valueOf("Shift+Insert")) {
							action {
								stmt.elseifGroups.addBefore(elseif, XlElseIf())
							}
						}
						item("Add Else-If Below", KeyCombination.valueOf("Insert")) {
							action {
								stmt.elseifGroups.addAfter(elseif, XlElseIf())
							}
						}
						item("Delete Else-If", KeyCombination.valueOf("Delete")) {
							textProperty().bind(bindingN(elseif.testProperty) { test ->
								"Delete Else-If" + test?.crop(20)
							})
							action {
								stmt.elseifGroups.remove(elseif)
							}
						}
						item("Add Else") {
							action {
								stmt.elseGroup = XlElse()
							}
							enableWhen(stmt.elseGroupProperty.isNull)
						}
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
				contextmenu {
					item("Delete Else", KeyCombination.valueOf("Delete")) {
						action {
							stmt.elseGroup = null
						}
					}
				}
			}
			presentWhen(stmt.elseGroupProperty.isNotNull)
		}
	})
}

