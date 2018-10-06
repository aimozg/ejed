package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsBattle
import tornadofx.*

class ScBattle(stmt: XsBattle) : StatementControl<XsBattle>(stmt) {
	override fun createDefaultSkin() = ScSkin<XsBattle, ScBattle>(this) {
		addClass(Styles.xnext)
		scFlow(Styles.xnext) {
			text("Battle with ")
			valueLink(stmt.monsterProperty, "Monster",
			          MonsterChooser(mod() ?: return@scFlow))
		}
	}
}