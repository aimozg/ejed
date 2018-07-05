package ej.editor.utils

import com.sun.javafx.font.PrismFontLoader
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
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

inline fun<T> UIComponent.textInputDialog(title:String,
                                          label:String,
                                          initialValue:String="",
                                          cancelable:Boolean=true,
                                          handler:(String)->T):T? {
	return textInputDialog(title, label, initialValue, cancelable)?.let(handler)
}
class TextInputDialog : View() {
	var result:String? = null
	
	val labelProperty = SimpleObjectProperty("")
	var label by labelProperty
	
	val initialValueProperty = SimpleObjectProperty("")
	var initialValue by initialValueProperty
	
	val cancelableProperty = SimpleObjectProperty(false)
	var cancelable by cancelableProperty
	
	fun showModal(title:String,label:String,initialValue:String="",cancelable:Boolean=true):String? {
		this.title = title
		this.label = label
		this.initialValue = initialValue
		this.cancelable = cancelable
		this.result = null
		openModal(block = true)
		return result
	}
	override val root = form {
		fieldset {
			lateinit var input: TextField
			field("Label") {
				label.textProperty().bind(labelProperty)
				input = textfield(initialValueProperty)
			}
			hbox(20) {
				//			alignment = Pos.BASELINE_CENTER
				button("Ok").action {
					result = input.text
					close()
				}
				button("Cancel").visibleWhen(cancelableProperty).action {
					result = null
					close()
				}
			}
		}
	}
}
fun textInputDialog(title:String,label:String,initialValue:String="",cancelable:Boolean=true):String? {
	return find<TextInputDialog>().showModal(title, label, initialValue, cancelable)
}