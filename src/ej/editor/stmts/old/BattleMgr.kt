package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.expr.defaultEditorTextFlow
import ej.editor.expr.valueLink
import ej.editor.stmts.MonsterChooser
import ej.mod.XsBattle
import tornadofx.*

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

object BattleMgr : StatementManager<XsBattle>() {
	override fun editorBody(stmt: XsBattle,
	                        tree: StatementTree
	) = defaultEditorTextFlow {
		text("Battle with ")
		valueLink(stmt.monsterProperty, "Monster",
		          MonsterChooser(controller.mod ?: return@defaultEditorTextFlow))
	}
	
	override fun treeGraphic(stmt: XsBattle, tree: StatementTree) = simpleTreeLabel(
			stmt.monsterProperty.stringBinding { "Battle with $it" }
	) {
		addClass(Styles.xnext)
	}
	
}