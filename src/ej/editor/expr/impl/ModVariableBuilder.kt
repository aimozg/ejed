package ej.editor.expr.impl

import ej.editor.expr.*
import ej.editor.utils.ListValueChooser
import ej.editor.utils.withPropertiesFrom
import ej.mod.StateVar
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.Pane
import tornadofx.*

class ModVariableBuilder : ExpressionBuilder() {
	override fun copyMe() = ModVariableBuilder().withPropertiesFrom(this,
	                                                                ModVariableBuilder::variable)
	override fun name() = "Mod state variable"
	
	override fun editorBody(): Pane = defaultEditorTextFlow {
		text("Mod variable ")
		valueLink(variable,
		          "Mod variable",
		          ListValueChooser(controller.mod?.stateVars ?: emptyList()) {
			          it?.name ?: "<???>"
		          })
	}
	
	override fun text() = mktext("Mod variable ", variable.value?.name)
	override fun build(): Expression {
		val value = variable.value ?: return nop()
		return if (ExpressionParser.isValidId(value.name)) {
			DotExpression(Identifier(KnownIds.MOD_STATE), value.name)
		} else {
			AccessExpression(Identifier(KnownIds.MOD_STATE), StringLiteral(value.name))
		}
	}
	
	val variable = SimpleObjectProperty<StateVar>()
	
	companion object : PartialBuilderConverter<Expression> {
		override fun tryConvert(converter: BuilderConverter, expr: Expression): ModVariableBuilder? {
			return when (expr) {
				is AccessExpression -> tryConvert(expr)
				is DotExpression -> tryConvert(expr)
				else -> null
			}
		}
		
		fun tryConvert(expr: AccessExpression): ModVariableBuilder? {
			if (expr.obj.asId?.value != KnownIds.MOD_STATE) return null
			val key = expr.obj.asStringLiteral?.value ?: return null
			return ModVariableBuilder().apply {
				variable.value = controller.mod?.stateVars?.firstOrNull { it.name == key } ?: return null
			}
		}
		
		fun tryConvert(expr: DotExpression): ModVariableBuilder? {
			if (expr.obj.asId?.value != KnownIds.MOD_STATE) return null
			return ModVariableBuilder().apply {
				variable.value = controller.mod?.stateVars?.firstOrNull { it.name == expr.key } ?: return null
			}
		}
		
	}
}