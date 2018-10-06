package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.presentWhen
import ej.editor.utils.stringValueToggler
import ej.mod.XlSwitch
import ej.mod.XlSwitchCase
import tornadofx.*

class ScSwitch(stmt: XlSwitch) : StatementControl<XlSwitch>(stmt) {
	override fun createDefaultSkin() = SwitchSkin()
	inner class SwitchSkin : ScSkin<XlSwitch, ScSwitch>(this, {
		addClass(Styles.xlogic)
		scFlow(Styles.xlogic) {
			text("Choose one of options")
			checkbox("using selector", stringValueToggler(stmt.valueProperty, "rand(100)"))
			textfield(stmt.valueProperty) {
				disableWhen { stmt.valueProperty.isNullOrEmpty() }
			}
		}
		simpleList(stmt.branches) { stmt ->
			stmtList(stmt.content) {
				beforeList = hbox {
					children += detachListMenu()
					scFlow(Styles.xlogic) {
						text("Branch when: ")
						combobox(
								stmt.conditionTypeProperty,
								XlSwitchCase.ConditionType.values().asList()
										- XlSwitchCase.ConditionType.OTHER
						) {
							cellFormat(DefaultScope) {
								text = when (item) {
									XlSwitchCase.ConditionType.NEVER, null ->
										"(never)"
									XlSwitchCase.ConditionType.TEST -> "condition is true:"
									XlSwitchCase.ConditionType.X_EQ_A -> "selector = "
									XlSwitchCase.ConditionType.X_NEQ_A -> "selector ≠ "
									XlSwitchCase.ConditionType.X_GT_A -> "selector > "
									XlSwitchCase.ConditionType.X_GTE_A -> "selector ≥ "
									XlSwitchCase.ConditionType.X_LT_A -> "selector < "
									XlSwitchCase.ConditionType.X_LTE_A -> "selector ≤ "
									XlSwitchCase.ConditionType.X_LTE_A_X_GTE_B -> "≤ selector ≤"
									XlSwitchCase.ConditionType.X_LT_A_X_GT_B -> "< selector <"
									XlSwitchCase.ConditionType.X_LTE_A_X_GT_B -> "≤ selector <"
									XlSwitchCase.ConditionType.X_LT_A_X_GTE_B -> "< selector ≤"
									XlSwitchCase.ConditionType.OTHER -> "(too complex condition)"
								}
							}
						}
						textfield(stmt.testProperty) {
							presentWhen(stmt.testProperty.isNotNull)
						}
						textfield(stmt.valueProperty) {
							presentWhen(stmt.valueProperty.isNotNull)
						}
						textfield(stmt.neProperty) {
							presentWhen(stmt.neProperty.isNotNull)
						}
						textfield(stmt.gtProperty) {
							presentWhen(stmt.gtProperty.isNotNull)
						}
						textfield(stmt.gteProperty) {
							presentWhen(stmt.gteProperty.isNotNull)
						}
						textfield(stmt.ltProperty) {
							presentWhen(stmt.ltProperty.isNotNull)
						}
						textfield(stmt.lteProperty) {
							presentWhen(stmt.lteProperty.isNotNull)
						}
					}
				}
			}
		}
	})
}