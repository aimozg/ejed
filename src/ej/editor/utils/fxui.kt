package ej.editor.utils

import com.sun.javafx.font.PrismFontLoader
import javafx.beans.value.ObservableValue
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import tornadofx.*


/*
 * Created by aimozg on 27.06.2018.
 * Confidential until published on GitHub
 */

fun <T : Node> T.colspan(value:Int): T {
	val gpc = GridPaneConstraint(this)
	gpc.columnSpan = value
	return gpc.applyToNode(this)
}

inline fun GridPane.smartRow(op: Pane.() -> Unit) {
	properties["TornadoFX.GridPaneRowId"] =
			(properties.getOrDefault("TornadoFX.GridPaneRowId",-1) as Int) + 1
	val fake = Pane()
	fake.properties["TornadoFX.GridPaneParentObject"] = this
	op(fake)
	val row = properties["TornadoFX.GridPaneRowId"] as Int
	var column = 0
	val newChildren = fake.children.toList()
	for (cell in newChildren) {
		column = GridPane.getColumnIndex(cell) ?: column
		val colspan = GridPane.getColumnSpan(cell) ?: 1
		GridPane.setConstraints(cell, column, row)
		column += colspan
	}
	children.addAll(newChildren)
}

fun <T:Fragment> T.initialized():T {
	init()
	return this
}

var TEXTAREA_HPAD = 16.0
fun TextArea.autoPrefRowCount():Int? {
	val width = (width.takeIf { it != 0.0 } ?: return null) - TEXTAREA_HPAD
	val text = text
	val fm = PrismFontLoader.getInstance().getFontMetrics(font)
	val tb = (lookup(".text") as? Text)?.boundsInLocal
	val fsz = Math.ceil(fm.lineHeight.toDouble())
	val th = if (tb != null && tb.width <= width) {
		tb.height
	} else {
		val tw = /*tb?.width ?:*/ fm.computeStringWidth(text).toDouble()
		Math.ceil(Math.ceil(tw / width + text.count { it == '\n' }).times(fsz))
	}
	
	val rc = Math.ceil(th / fsz)
	if (!rc.isFinite()) return null
	return rc.toInt()
}
fun TextArea.autoStretch() {
	maxHeight = Control.USE_PREF_SIZE
	prefHeight = Control.USE_COMPUTED_SIZE
	
	prefRowCountProperty().bind(
			bindingN(textProperty(), widthProperty(), fontProperty()) { _, _, _ ->
				autoPrefRowCount() ?: 1
			})
	prefRowCountProperty().onChange {
		runLater { requestLayout() }
	}
}

fun TextArea.stretchOnFocus(unfocusedRowCount: Int = 1) {
	//prefRowCount = maxOf(1, minOf(unfocusedRowCount, text.count { it=='\n' }+1))
	prefRowCountProperty().bind(
			bindingN(focusedProperty(), textProperty(), widthProperty()) { focused, _, _ ->
				val rc = autoPrefRowCount() ?: 1
				if (focused == true) rc else minOf(unfocusedRowCount, rc)
			}
	)
}

@Suppress("DEPRECATION")
fun Node.dumpCss() {
	println(impl_getStyleMap().map {(k,v)->
		k.cssMetaData.property+" "+v.joinToString { it.selector.toString() }
	}.joinToString("\n"))
}

inline fun<T> TreeItem<T>.traverseAll(visitor:(TreeItem<T>)->Unit) {
	traverse {
		visitor(it)
		true
	}
}
inline fun<T> TreeItem<T>.traverse(visitor:(TreeItem<T>)->Boolean) {
	var e:TreeItem<T>? = this
	while(e != null) {
		var next = e.nextSibling()
		if (visitor(e)) {
			next = e.children.firstOrNull() ?: next
		}
		while (next == null && e != null) {
			e = e.parent
			next = e?.nextSibling()
		}
		e = next
	}
}

data class ContextualTreeSelection<T>(val item:TreeItem<T>,val parent:TreeItem<T>?,val siblings:List<TreeItem<T>>?) {
	val value:T? get() = item.value
	val inRoot = parent?.parent == null
	constructor(item:TreeItem<T>):this(item,item.parent,item.parent?.children)
}

fun<T> TreeView<T>.itemForValue(value:T):TreeItem<T>? {
	var result:TreeItem<T>? = null
	root.traverse {
		if (it.value == value) {
			result = it
			false
		} else true
	}
	return result
}
inline fun<T> TreeView<T>.findItem(filter:(T)->Boolean):TreeItem<T>? {
	root.traverseAll {
		if (filter(it.value)) {
			return it
		}
	}
	return null
}
inline fun <T> TreeView<T>.select(andScroll:Boolean = true,filter:(T)->Boolean) {
	findItem(filter)?.let { item ->
		selectionModel.select(item)
		if (andScroll) scrollTo(getRow(item))
	}
}

inline fun <T> textInputDialog(title: String,
                               label: String,
                               initialValue: String = "",
                               cancelable: Boolean = true,
                               handler: (String) -> T): T? {
	return textInputDialog(title, label, initialValue, cancelable)?.let(handler)
}

fun textInputDialog(
		title: String,
		label: String,
		initialValue: String = "",
		cancelable: Boolean = true): String? {
	val dialog = TextInputDialog(initialValue)
	dialog.title = title
//	dialog.headerText = "Look, a Text Input Dialog"
	dialog.dialogPane.header = HBox()
	dialog.contentText = label
	if (!cancelable) {
		dialog.dialogPane.buttonTypes.remove(ButtonType.CANCEL)
	}
	return dialog.showAndWait().orElse(null)
}


/**
 * This extension function will make sure that the given node will only be visible in the scene graph,
 * if the given [expr] returning an observable boolean value equals true.
 */
fun <T : Node> T.presentWhen(expr: () -> ObservableValue<Boolean>): T = presentWhen(expr())

/**
 * This extension function will make sure that the given node will only be visible in the scene graph,
 * if the given [predicate] observable boolean value equals true.
 */
fun <T : Node> T.presentWhen(predicate: ObservableValue<Boolean>) = apply {
	visibleProperty().cleanBind(predicate)
	managedProperty().cleanBind(predicate)
}

inline fun <reified T : Node> Node.ancestor(): T? = generateSequence(this) {
	it.parent
}.filterIsInstance<T>().firstOrNull()