package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.mod.XsBattle
import javafx.scene.layout.VBox
import tornadofx.*

class ScBattle(stmt: XsBattle) : StatementControl<XsBattle>(stmt) {
	override fun createDefaultSkin() = BattleSkin()
	inner class BattleSkin : ScSkin<XsBattle, ScBattle>(this) {
		override fun VBox.body() {
			addClass(Styles.xnext)
			scFlow(Styles.xnext) {
				text("Battle with ")
				valueLink(stmt.monsterProperty, "Monster",
				          MonsterChooser(mod ?: return@scFlow))
			}
		}
	}
}