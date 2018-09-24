package ej.editor.stmts

import ej.editor.expr.defaultEditorTextFlow
import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.ancestor
import ej.editor.views.SceneEditor
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Orientation
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
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	fun rootStatement(): XComplexStatement? {
		return ancestor<SceneEditor>()?.rootStatement
	}
	
	fun mod(): ModData? {
		return ancestor<SceneEditor>()?.mod
	}
	abstract override fun createDefaultSkin(): Skin<*>
	abstract class ScSkin<S : XStatement, C : StatementControl<S>>(control: C) : SingleElementSkinBase<C>(control) {
		final override val main: Node
		abstract fun VBox.body()
		
		init {
			main = VBox().apply {
				addClass("stmt-ctrl-main")
				body()
				this@ScSkin.children += this
			}
		}
		
		inline fun Parent.scRow(init: HBox.() -> Unit) = HBox().apply {
			addClass("stmt-ctrl-row")
			hgrow = Priority.ALWAYS
			init()
			this@scRow.add(this)
		}
		
		inline fun Parent.scFlow(cssClass: CssRule? = null,
		                         init: TextFlow.() -> Unit) = defaultEditorTextFlow(cssClass) {
			init()
			this@scFlow.add(this)
		}
		
		inline fun Parent.stmtList(items: ObservableList<XStatement>,
		                           init: StatementListView.() -> Unit = {}) = StatementListView(items).apply {
			init()
			this@stmtList.add(this)
		}
		
		inline fun Parent.stmtList(items: ObservableValue<ObservableList<XStatement>>,
		                           init: StatementListView.() -> Unit = {}) = StatementListView(items).apply {
			init()
			this@stmtList.add(this)
		}
		
		fun <T : Any> Parent.simpleList(cellDecorator: VBox.(T) -> Node) = SimpleListView<T>().apply {
			graphicFactory {
				VBox().apply {
					cellDecorator(it)
				}
			}
			this@simpleList.add(this)
		}
		
		fun <T : Any> Parent.simpleList(pItems: ObservableList<T>, cellDecorator: VBox.(T) -> Node) = simpleList(
				cellDecorator).apply {
			this.items = pItems
		}
		
		fun <T : Any> Parent.simpleList(pItems: ObservableValue<ObservableList<T>>,
		                                cellDecorator: VBox.(T) -> Node) = simpleList(cellDecorator).apply {
			this.itemsProperty.bind(pItems)
		}
	}
}

@Suppress("UNCHECKED_CAST")
fun <T : XStatement> T.createControl(): StatementControl<T>? = when (this) {
	is XsBattle -> ScBattle(this)
	is XsDisplay -> ScDisplay(this)
	is XsForward -> ScForward(this)
	is XsOutput -> ScOutput(this)
	is XsSet -> ScSet(this)
	is XsMenu -> ScMenu(this)
	is XsNext -> ScNext(this)
	is XsButton -> ScButton(this)
	is XlIf -> ScIf(this)
	is XlComment -> ScComment(this)
	is XlSwitch -> ScSwitch(this)
	is XcText -> ScText(this)
	else -> null
} as StatementControl<T>?

