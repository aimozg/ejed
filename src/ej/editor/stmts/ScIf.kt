package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.lists.BoolExprChooser
import ej.editor.expr.valueLink
import ej.editor.utils.ContextMenuContainer
import ej.editor.utils.bindingN
import ej.editor.utils.observableUnique
import ej.editor.utils.presentWhen
import ej.mod.*
import ej.utils.addAfter
import ej.utils.addBefore
import ej.utils.crop
import javafx.scene.control.Menu
import javafx.scene.input.KeyCombination
import javafx.scene.layout.HBox
import tornadofx.*

/*
 * Created by aimozg on 22.09.2018.
 * Confidential until published on GitHub
 */
class ScIf(stmt: XlIf) : StatementControl<XlIf>(stmt), ContextMenuContainer {
	override fun createDefaultSkin() = IfSkin()
	
	override val menus: List<Menu> by lazy {
		listOf(Menu("_If-Then-Else").apply {
			item("Add Else-If") {
				action {
					stmt.elseifGroups.add(XlElseIf().also { it.content += XcText() })
				}
			}
			item("Add Else") {
				action {
					stmt.elseGroup = XlElse().also { it.content += XcText() }
				}
				enableWhen(stmt.elseGroupProperty.isNull)
			}
			item("Delete Else") {
				action {
					stmt.elseGroup = null
				}
				enableWhen(stmt.elseGroupProperty.isNotNull)
			}
		})
	}
	
	inner class IfSkin : ScSkin<XlIf, ScIf>(this, {
		addClass("sc-if")
		addClass(Styles.xlogic)
		stmtList(stmt.thenGroup.content) {
			addClass("sc-if-then")
			contentBeforeList += HBox().apply {
				children += listTopMenu
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
				contentBeforeList += HBox().apply {
					children += listTopMenu
					scFlow(Styles.xlogic) {
						text("Else if ")
						valueLink("Condition", elseif.testProperty.toBuilder(), BoolExprChooser, setter = {
							if (it != null) elseif.testProperty.fromBuilder(it)
						})
					}
					contextmenu {
						item("Add Else-If Above", KeyCombination.valueOf("Shift+Insert")) {
							action {
								stmt.elseifGroups.addBefore(elseif, XlElseIf().also { it.content += XcText() })
							}
						}
						item("Add Else-If Below", KeyCombination.valueOf("Insert")) {
							action {
								stmt.elseifGroups.addAfter(elseif, XlElseIf().also { it.content += XcText() })
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
								stmt.elseGroup = XlElse().also { it.content += XcText() }
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
			contentBeforeList += HBox().apply {
				children += listTopMenu
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

