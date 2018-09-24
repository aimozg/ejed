package ej.editor.stmts

import ej.editor.expr.defaultEditorTextFlow
import ej.editor.utils.SingleElementSkinBase
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.control.Control
import javafx.scene.control.Skin
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import tornadofx.*

/*
 * Created by aimozg on 21.09.2018.
 * Confidential until published on GitHub
 */
abstract class StatementControl<T : XStatement>(val stmt: T) : Control() {
	var mod: ModData? = null
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	abstract override fun createDefaultSkin(): Skin<*>
	abstract class ScSkin<S : XStatement, C : StatementControl<S>>(control: C) : SingleElementSkinBase<C>(control) {
		final override val main: Node
		abstract fun VBox.body()
		
		init {
			main = VBox().apply {
				isFillWidth = true
				spacing = 2.0
				body()
				this@ScSkin.children += this
			}
		}
		
		inline fun VBox.scRow(init: HBox.() -> Unit) = HBox().apply {
			hgrow = Priority.ALWAYS
			alignment = Pos.BASELINE_LEFT
			spacing = 2.0
			init()
			this@scRow.add(this)
		}
		
		inline fun Parent.scFlow(cssClass: CssRule? = null,
		                         init: TextFlow.() -> Unit) = defaultEditorTextFlow(cssClass) {
			init()
			this@scFlow.add(this)
		}
		
		inline fun Parent.stmtList(items: ObservableList<XStatement>,
		                           init: StatementListView.() -> Unit) = StatementListView(items, skinnable.mod).apply {
			init()
			this@stmtList.add(this)
		}
		
		inline fun Parent.stmtList(items: ObservableValue<ObservableList<XStatement>>,
		                           init: StatementListView.() -> Unit) = StatementListView(items, skinnable.mod).apply {
			init()
			this@stmtList.add(this)
		}
		
	}
}

@Suppress("UNCHECKED_CAST")
fun <T : XStatement> T.createControl(): StatementControl<T>? = when (this) {
	is XsBattle -> ScBattle(this)
//	is XsDisplay -> ScDisplay(this)
//	is XsForward -> ScForward(this)
//	is XsOutput -> ScOutput(this)
	is XsSet -> ScSet(this)
//	is XsMenu -> ScMenu(this)
//	is XsNext -> ScNext(this)
//	is XsButton -> ScButton(this)
	is XlIf -> ScIf(this)
//	is XlComment -> ScComment(this)
//	is XlSwitch -> ScSwitch(this)
	is XcText -> ScText(this)
	else -> null
} as StatementControl<T>?

