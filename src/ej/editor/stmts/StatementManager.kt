package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.StatementTree
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.control.Labeled
import javafx.scene.control.TextField
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

inline fun defaultEditorBody(init:HBox.()->Unit):HBox = defaultEditorBody(HBox(),init)
inline fun<T:Pane> defaultEditorBody(pane:T, init:T.()->Unit):T =
		pane.apply {
			addClass(Styles.xstmtEditor)
			hgrow = Priority.ALWAYS
			(when (this) {
				is HBox -> alignmentProperty()
				is VBox -> alignmentProperty()
				is FlowPane -> alignmentProperty()
				is TilePane -> alignmentProperty()
				is GridPane -> alignmentProperty()
				is StackPane -> alignmentProperty()
				is Labeled -> alignmentProperty()
				is TextField -> alignmentProperty()
				else -> null
			})?.set(Pos.BASELINE_LEFT)
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
//	XsBattle::class.java -> BattleMgr TODO
	XsDisplay::class.java -> DisplayMgr
	XsOutput::class.java -> OutputMgr
	XsSet::class.java -> SetMgr
	XsMenu::class.java -> MenuMgr
	XsNext::class.java -> NextMgr
	XsButton::class.java -> ButtonMgr
	XlIf::class.java -> IfMgr
	XlThen::class.java -> ThenMgr
	XlElseIf::class.java -> ElseIfMgr
	XlElse::class.java -> ElseMgr
	XlComment::class.java -> CommentMgr
	XlSwitch::class.java -> SwitchMgr
	XlSwitchCase::class.java -> SwitchCaseMgr
	XlSwitchDefault::class.java -> SwitchDefaultMgr
	XcText::class.java -> TextMgr
	else -> null
} as StatementManager<T>?

inline fun<reified T:XStatement> statementManager():StatementManager<T>? = T::class.java.statementManager()
fun<T:XStatement> T.manager():StatementManager<T>? = javaClass.statementManager()