package ej.editor.utils

import javafx.beans.Observable
import javafx.beans.binding.Bindings
import javafx.beans.value.ObservableValue
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.StackPane
import tornadofx.*
import java.util.concurrent.Callable

/*
 * Created by aimozg on 04.10.2018.
 * Confidential until published on GitHub
 */
class NodeBinding(
		val observable: ObservableValue<Node?>) : StackPane() {
	private var body: Node? = null
	
	init {
		alignment = Pos.TOP_LEFT
		isFocusTraversable = false
	}
	
	private val listener = observable.onChangeAndNowWeak { newBody ->
		val oldBody = children.indexOf(body).takeIf { it >= 0 }
		body = newBody
		if (newBody != null) {
			if (oldBody != null) children[oldBody] = newBody
			else children.add(newBody)
		} else {
			if (oldBody != null) children.removeAt(oldBody)
		}
		requestLayout()
	}
}

fun Parent.nodeBinding(observable: Observable, _createGraphic: () -> Node?): NodeBinding {
	return NodeBinding(Bindings.createObjectBinding(Callable(_createGraphic), observable)).attachTo(this)
}

fun Parent.nodeBinding(observable: ObservableValue<Node?>): NodeBinding {
	return NodeBinding(observable).attachTo(this)
}
fun <T> Parent.nodeBinding(observable: ObservableValue<T>, _createGraphic: (T?) -> Node?): NodeBinding {
	return NodeBinding(observable.objectBinding { _createGraphic(it) }).attachTo(this)
}

fun <T> Parent.nodeBindingNonNull(observable: ObservableValue<T>, _createGraphic: (T) -> Node?): NodeBinding {
	return NodeBinding(Bindings.createObjectBinding(Callable { _createGraphic(observable.value) })).attachTo(this)
}


