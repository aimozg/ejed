package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.presentWhen
import ej.editor.utils.stringValueToggler
import ej.mod.XlSwitch
import javafx.scene.layout.VBox
import tornadofx.*

class ScSwitch(stmt: XlSwitch) : StatementControl<XlSwitch>(stmt) {
	override fun createDefaultSkin() = SwitchSkin()
	inner class SwitchSkin : ScSkin<XlSwitch, ScSwitch>(this) {
		override fun VBox.body() {
			addClass(Styles.xlogic)
			scFlow(Styles.xlogic) {
				text("Choose one of options")
				checkbox("using selector", stringValueToggler(stmt.valueProperty, "rand(100)"))
				textfield(stmt.valueProperty) {
					disableWhen { stmt.valueProperty.isNullOrEmpty() }
				}
			}
			simpleList(stmt.branches) { stmt ->
				translateX = 6.0
				gridpane {
					hgap = 2.0
					vgap = 2.0
					addClass(Styles.xlogic)
					row {
						text("Branch when: ")
						checkbox(" ", stringValueToggler(stmt.testProperty, "true"))
						text(" and selector ")
						checkbox("= ", stringValueToggler(stmt.valueProperty, "0"))
						checkbox("≠ ", stringValueToggler(stmt.neProperty, "0"))
						checkbox("> ", stringValueToggler(stmt.gtProperty, "50"))
						checkbox("≥ ", stringValueToggler(stmt.gteProperty, "50"))
						checkbox("< ", stringValueToggler(stmt.ltProperty, "50"))
						checkbox("≤ ", stringValueToggler(stmt.lteProperty, "50"))
						
					}
					row {
						hbox {}
						textfield(stmt.testProperty) {
							presentWhen(stmt.testProperty.isNotBlank())
						}
						hbox {}
						textfield(stmt.valueProperty) {
							presentWhen(stmt.valueProperty.isNotBlank())
						}
						textfield(stmt.neProperty) {
							presentWhen(stmt.neProperty.isNotBlank())
						}
						textfield(stmt.gtProperty) {
							presentWhen(stmt.gtProperty.isNotBlank())
						}
						textfield(stmt.gteProperty) {
							presentWhen(stmt.gteProperty.isNotBlank())
						}
						textfield(stmt.ltProperty) {
							presentWhen(stmt.ltProperty.isNotBlank())
						}
						textfield(stmt.lteProperty) {
							presentWhen(stmt.lteProperty.isNotBlank())
						}
					}
				}
				stmtList(stmt.content) {
					translateX = 12.0
				}
			}
			
		}
	}
}