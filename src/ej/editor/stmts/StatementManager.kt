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
fun<T:XStatement> Class<T>.statementManager():StatementManager<T>? = when(this) {
	XsDisplay::class.java -> DisplayMgr as StatementManager<T>
	XsOutput::class.java -> OutputMgr as StatementManager<T>
	XsSet::class.java -> SetMgr as StatementManager<T>
	XlIf::class.java -> IfMgr as StatementManager<T>
	XlElseIf::class.java -> ElseIfMgr as StatementManager<T>
	XlElse::class.java -> ElseMgr as StatementManager<T>
	XlComment::class.java -> CommentMgr as StatementManager<T>
	XcText::class.java -> TextMgr as StatementManager<T>
	else -> null
}

inline fun<reified T:XStatement> statementManager():StatementManager<T>? = T::class.java.statementManager()
fun<T:XStatement> T.manager():StatementManager<T>? = javaClass.statementManager()