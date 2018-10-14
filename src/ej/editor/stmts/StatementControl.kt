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
import javafx.scene.control.ContextMenu
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
	
	fun mergedContextMenu(): ContextMenu {
		val cm = ancestor<SimpleListView.SimpleListCell<*>>()?.contextMenu
		if (cm != null) return cm
		return contextmenu { }
	}
	
	fun mod(): ModData? {
		return ancestor<SceneEditor>()?.mod
	}
	abstract override fun createDefaultSkin(): Skin<*>
	
	init {
		isFocusTraversable = false
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
	class Entry(val name: String, val hotkey: String, val factory: () -> XStatement)
	
	val entries: List<Entry?> = listOf(
			Entry("Te_xt", "X") { XcText() },
			Entry("_//Comment", "/") { XlComment() },
			Entry("_Display", "D") { XsDisplay() },
			Entry("O_utput", "U") { XsOutput() },
			Entry("_Command", "C") { XsCommand() },
			null,
			Entry("Set _=", "=") { XsSet() },
			Entry("_If-Then-Else", "I") { XlIf() },
			Entry("_Switch-Branches", "S") { XlSwitch() },
			null,
			Entry("_Next", "N") { XsNext() },
			Entry("_Menu", "M") { XsMenu() },
			Entry("Menu _Button", "B") { XsButton() },
			Entry("_Forward", "F") { XsForward() },
			Entry("Batt_le", "L") { XsBattle() }
	)
}