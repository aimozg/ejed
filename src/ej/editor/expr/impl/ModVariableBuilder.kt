package ej.editor.expr.impl

import ej.editor.expr.*
import ej.mod.StateVar
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class ModVariableBuilder : ExpressionBuilder() {
	override fun name() = "Mod state variable"
	
	override fun editorBody(): Pane = defaultBuilderBody {
		text("Mod variable ")
		valueLink(variable,
		          "Mod variable",
		          ListValueChooser(controller.mod?.stateVars ?: emptyList()) {
			          it?.name ?: "<???>"
		          })
	}
	
	override fun text() = mktext("Mod variable ", variable.value?.name)
	override fun build() = DotExpression(Identifier("state"), variable.value.name)
	
	val variable = SimpleObjectProperty<StateVar>()
	
	companion object : PartialBuilderConverter<DotExpression> {
		override fun tryConvert(converter: BuilderConverter, expr: DotExpression): ModVariableBuilder? {
			if (expr.obj.asId?.value != "state") return null
			return ModVariableBuilder().apply {
				variable.value = controller.mod?.stateVars?.firstOrNull { it.name == expr.key } ?: return null
			}
		}
		
	}
}