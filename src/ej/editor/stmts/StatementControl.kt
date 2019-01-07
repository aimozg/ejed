package ej.editor.stmts

import ej.editor.expr.defaultEditorTextFlow
import ej.editor.utils.SimpleListView
import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.ancestor
import ej.editor.views.SceneEditor
import ej.mod.*
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Orientation
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
	
	init {
		isFocusTraversable = false
		addClass("stmt-ctrl")
	}
	
	open class ScSkin<S : XStatement, C : StatementControl<S>>(control: C,
	                                                           mainBody: VBox.() -> Unit) : SingleElementSkinBase<C>(
			control,
			VBox().apply(mainBody)) {
		
		init {
			main.addClass("stmt-ctrl-main")
		}
	}
	
	inline fun Parent.scRow(init: HBox.() -> Unit) = HBox().apply {
		addClass("stmt-ctrl-row")
		hgrow = Priority.ALWAYS
		init()
	}.attachTo(this)
	
	inline fun Parent.scFlow(cssClass: CssRule? = null,
	                         init: TextFlow.() -> Unit) = defaultEditorTextFlow(cssClass) {
		init()
	}.attachTo(this)
	
	inline fun Parent.stmtList(items: ObservableList<XStatement>,
	                           init: StatementListView.() -> Unit = {}) = StatementListView().apply {
		this.items = items
		init()
	}.attachTo(this)
	
	inline fun Parent.stmtList(items: ObservableValue<ObservableList<XStatement>>,
	                           init: StatementListView.() -> Unit = {}) = StatementListView().apply {
		this.itemsProperty.bind(items)
		init()
	}.attachTo(this)
	
	fun <T : Any> Parent.simpleList(cellDecorator: VBox.(T) -> Unit) = SimpleListView<T>().apply {
		graphicFactory { stmt ->
			VBox().apply {
				cellDecorator(stmt)
			}
		}
	}.attachTo(this)
	
	fun <T : Any> Parent.simpleList(
			pItems: ObservableList<T>,
			cellDecorator: VBox.(T) -> Unit
	) = simpleList(cellDecorator).apply {
		this.items = pItems
	}
	
	fun <T : Any> Parent.simpleList(pItems: ObservableValue<ObservableList<T>>,
	                                cellDecorator: VBox.(T) -> Unit) = simpleList(cellDecorator).apply {
		this.itemsProperty.bind(pItems)
	}
}

fun XStatement.createControl(): StatementControl<*>? = when (this) {
	is XsBattle -> ScBattle(this)
	is XsDisplay -> ScDisplay(this)
	is XsForward -> ScForward(this)
	is XsOutput -> ScOutput(this)
	is XsSet -> ScSet(this)
	is XsCommand -> ScCommand(this)
	is XsMenu -> ScMenu(this)
	is XsNext -> ScNext(this)
	is XsButton -> ScButton(this)
	is XlIf -> ScIf(this)
	is XlComment -> ScComment(this)
	is XlSwitch -> ScSwitch(this)
	is XcText -> ScText(this)
	else -> null
}

object StatementMetadata {
	class Entry(val name: String, val hotkey: String?, val factory: () -> XStatement)
	
	val entries: List<Entry?> = listOf(
			Entry("Te_xt", "1") { XcText() },
			Entry("_//Comment", "2") { XlComment() },
			Entry("_Display", "3") { XsDisplay() },
			Entry("O_utput", "4") { XsOutput() },
			Entry("_Command", "5") { XsCommand() },
			null,
			Entry("Set _=", "6") { XsSet() },
			Entry("_If-Then-Else", "7") {
				XlIf().also { it.thenGroup.content += XcText() }
			},
			Entry("_Switch-Branches", "8") { XlSwitch() },
			null,
			Entry("_Next", "9") { XsNext() },
			Entry("_Menu", null) { XsMenu() },
			Entry("Menu _Button", null) { XsButton() },
			Entry("_Forward", null) { XsForward() },
			Entry("Batt_le", null) { XsBattle() }
	)
}