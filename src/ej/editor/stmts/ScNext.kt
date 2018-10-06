package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsNext
import ej.mod.acceptsMenu
import tornadofx.*

class ScNext(stmt: XsNext) : StatementControl<XsNext>(stmt) {
	override fun createDefaultSkin() = NextSkin()
	
	inner class NextSkin : ScSkin<XsNext, ScNext>(this, {
			scFlow(Styles.xnext) {
				text("[Next] --> ")
				valueLink(stmt.refProperty,
				          "Scene",
				          SceneChooser(rootStatement() ?: return@scFlow,
				                       mod() ?: return@scFlow) { !it.acceptsMenu })
			}
	})
	
}