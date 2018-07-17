package ej.editor.stmts

import ej.editor.Styles
import ej.editor.expr.valueLink
import ej.editor.views.StatementTree
import ej.mod.XsBattle
import javafx.scene.text.TextFlow
import tornadofx.*

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */

object BattleMgr : StatementManager<XsBattle>() {
	override fun editorBody(stmt: XsBattle,
	                        tree: StatementTree
	) = defaultEditorBody(TextFlow()){
		text("Battle with ")
		valueLink(stmt.monsterProperty, "Monster",
		          MonsterChooser(controller.mod ?: return@defaultEditorBody))
	}
	
	override fun treeGraphic(stmt: XsBattle, tree: StatementTree) = simpleTreeLabel(
			stmt.monsterProperty.stringBinding{"Battle with $it"}
	) {
		addClass(Styles.xcommand)
	}
	
}