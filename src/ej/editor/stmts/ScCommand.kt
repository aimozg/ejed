package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.ExpressionTypes
import ej.editor.expr.external.Stdlib
import ej.editor.expr.lists.SimpleExpressionChooser
import ej.editor.expr.valueLink
import ej.mod.XsCommand
import tornadofx.*

class ScCommand(stmt: XsCommand) : StatementControl<XsCommand>(stmt) {
	override fun createDefaultSkin() = ScSkin(this) {
		addClass(Styles.xcommand)
		scFlow {
			addClass(Styles.xcommand)
			valueLink(stmt.valueProperty.builderProperty,
			          "Command",
			          SimpleExpressionChooser(
					          Stdlib.commandBuilders(),
					          ExpressionTypes.VOID
			          )
			)
		}
	}
}