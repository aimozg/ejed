package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.*
import tornadofx.*

/*
 * Created by aimozg on 07.07.2018.
 * Confidential until published on GitHub
 */

abstract class StatementManager<T:XStatement> {
	abstract fun editorBody(stmt:T):Pane
	abstract fun treeGraphic(stmt:T, tree: StatementTree): Region
}

inline fun defaultEditorBody(init:HBox.()->Unit):HBox =
		HBox().apply {
			addClass(Styles.xstmtEditor)
			hgrow = Priority.ALWAYS
			alignment = Pos.BASELINE_LEFT
			init()
		}
inline fun vboxEditorBody(init:VBox.()->Unit):VBox =
		VBox().apply {
			addClass(Styles.xstmtEditor)
			hgrow = Priority.ALWAYS
			alignment = Pos.BASELINE_LEFT
			init()
		}

inline fun simpleTreeLabel(text:String="",init: Label.()->Unit={}):Label =
		Label(text).apply {
			init()
		}
inline fun simpleTreeLabel(text:ObservableValue<String>,init: Label.()->Unit={}):Label =
		Label().apply {
			textProperty().bind(text)
			init()
		}

@Suppress("UNCHECKED_CAST")
fun<T:XStatement> T.manager():StatementManager<T>? = when(this) {
	is XsDisplay -> DisplayMgr as StatementManager<T>
	is XsOutput -> OutputMgr as StatementManager<T>
	is XsSet -> SetMgr as StatementManager<T>
	is XlIf -> IfMgr as StatementManager<T>
	is XlElseIf -> ElseIfMgr as StatementManager<T>
	is XlElse -> ElseMgr as StatementManager<T>
	is XcText -> TextMgr as StatementManager<T>
	else -> null
}