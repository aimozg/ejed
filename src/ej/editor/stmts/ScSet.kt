package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.lists.AnyExprChooser
import ej.editor.utils.NullableStringConverter
import ej.editor.utils.isNullOrEmpty
import ej.editor.utils.stringValueToggler
import ej.mod.XsSet
import javafx.scene.layout.VBox
import tornadofx.*

class ScSet(stmt: XsSet) : StatementControl<XsSet>(stmt) {
	override fun createDefaultSkin() = SetSkin()
	
	inner class SetSkin : ScSkin<XsSet, ScSet>(this) {
		override fun VBox.body() {
			addClass(Styles.xcommand)
			scFlow {
				addClass(Styles.xcommand)
				label(stmt.opProperty.stringBinding {
					when (it) {
						null, "=", "assign" -> "Set to "
						"+", "+=", "add" -> "Add "
						"-" -> "Subtract "
						"*" -> "Multiply by "
						"/" -> "Divide by "
						else -> it
					}
				})
				textfield(stmt.valueProperty)
				button("...") {
					action {
						AnyExprChooser.pickValue("Value", stmt.valueProperty.toBuilder())?.let { v ->
							stmt.valueProperty.fromBuilder(v)
						}
					}
				}
				label(stmt.opProperty.stringBinding {
					when (it) {
						null, "=", "assign",
						"*", "/" -> "property"
						"+", "+=", "add" -> "to property"
						"-" -> "from property "
						else -> "property"
					}
				})
				textfield(stmt.varnameProperty) {
					prefColumnCount = 6
				}
				checkbox("of object", stringValueToggler(stmt.inobjProperty, "mod"))
				textfield(stmt.inobjProperty, NullableStringConverter) {
					disableWhen { stmt.inobjProperty.isNullOrEmpty() }
					prefColumnCount = 6
				}
			}
		}
	}
}