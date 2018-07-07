package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.XStatement
import ej.mod.XlIf
import ej.mod.XsDisplay
import ej.mod.XsOutput
import javafx.geometry.Pos
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

abstract class StatementManager<T:XStatement> {
	abstract fun editorBody(stmt:T):StmtEditorBody
	abstract fun treeGraphic(stmt:T, tree: StatementTree): StmtEditorLabel
}

open class StmtEditorBody(val stmt:XStatement) : HBox() {
	init {
		addClass(Styles.xstmtEditor)
		hgrow = Priority.ALWAYS
		alignment = Pos.BASELINE_LEFT
	}
}
inline fun StmtEditorBody(stmt:XStatement,init:StmtEditorBody.()->Unit):StmtEditorBody =
		StmtEditorBody(stmt).apply(init)
open class StmtEditorLabel(val stmt:XStatement):HBox() {

}
inline fun StmtEditorLabel(stmt:XStatement,init:StmtEditorLabel.()->Unit):StmtEditorLabel =
		StmtEditorLabel(stmt).apply(init)


@Suppress("UNCHECKED_CAST")
fun<T:XStatement> T.manager():StatementManager<T>? = when(this) {
	is XsDisplay -> DisplayMgr as StatementManager<T>
	is XsOutput -> OutputMgr as StatementManager<T>
	is XlIf -> IfMgr as StatementManager<T>
	else -> null
}