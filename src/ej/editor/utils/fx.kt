package ej.editor.utils

import com.sun.javafx.font.PrismFontLoader
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.WeakChangeListener
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.TreeItem
import javafx.scene.layout.GridPane
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

fun TextArea.stretchOnFocus(max:Int=1) {
	var rc = maxOf(1, minOf(max, text.count { it=='\n' }+1))
	fun fit(focused:Boolean):Int {
		val text = lookup(".text") as? Text
		return if (text == null) max else {
			val th = text.boundsInLocal.height
			val fm = PrismFontLoader.getInstance().getFontMetrics(text.font)
			val fsz = (Math.ceil(fm.descent.toDouble()) + Math.ceil(fm.leading.toDouble()) + Math.ceil(fm.ascent.toDouble()))
			rc = Math.ceil(th / fsz).toInt()
			if (!focused) minOf(max, rc) else rc
		}
	}
	prefRowCount = rc
	prefRowCountProperty().bind(
			focusedProperty().integerBinding(textProperty()) {
				if (it != null) fit(it) else rc
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
		if (visitor(e)) {
			e = e.children.firstOrNull() ?: e
		}
		var ie = e.nextSibling()
		while (ie == null && e != null) {
			e = e.parent
			ie = e.nextSibling()
		}
	}
}


fun <T> ObservableValue<T>.onChangeAndNowWeak(op: (T?) -> Unit): ChangeListener<T> {
	op(value)
	val listener = ChangeListener<T> { _, _, newValue -> op(newValue) }
	addListener(WeakChangeListener(listener))
	return listener
}
fun <T> ObservableValue<T>.onChangeAndNow(op: (T?) -> Unit) {
	op(value)
	addListener{ _, _, newValue -> op(newValue) }
}
fun <T> ObservableValue<T>.onChangeWeak(op: (T?) -> Unit): ChangeListener<T> {
	val listener = ChangeListener<T>{ _, _, newValue -> op(newValue) }
	addListener(WeakChangeListener(listener))
	return listener
}