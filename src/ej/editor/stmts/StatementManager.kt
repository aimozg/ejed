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
import javafx.scene.layout.Region
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

abstract class StatementManager<T:XStatement> {
	abstract fun editorBody(stmt:T):StmtEditorBody<T>
	abstract fun treeGraphic(stmt:T, tree: StatementTree): Region
}

open class StmtEditorBody<T:XStatement> : HBox() {
	init {
		addClass(Styles.xstmtEditor)
		alignment = Pos.BASELINE_LEFT
		hgrow = Priority.ALWAYS
		spacing = 2.0
	}
}
inline fun<T:XStatement> StmtEditorBody(init:StmtEditorBody<T>.()->Unit):StmtEditorBody<T> =
		StmtEditorBody<T>().apply(init)

@Suppress("UNCHECKED_CAST")
fun<T:XStatement> T.manager():StatementManager<T>? = when(this) {
	is XsDisplay -> DisplayMgr as StatementManager<T>
	is XsOutput -> OutputMgr as StatementManager<T>
	is XlIf -> IfMgr as StatementManager<T>
	else -> null
}