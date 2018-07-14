package ej.editor.expr

import ej.mod.StateVar
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class ModVariableBuilder : ExpressionBuilder() {
	override fun name() = "Mod state variable"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		text("Mod variable ")
		valueLink(variable,
		          ListValueChooser(controller.mod?.stateVars?: emptyList()) {
			          it?.name?:"<???>"
		          })
	}
	
	override fun text() = mktext("Mod variable ",variable.value?.name)
	override fun build() = DotExpression(Identifier("state"), variable.value.name)
	
	val variable = SimpleObjectProperty<StateVar>()
}