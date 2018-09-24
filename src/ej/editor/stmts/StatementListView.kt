package ej.editor.stmts

import ej.mod.XStatement
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.scene.control.Label
import javafx.scene.text.TextAlignment
import tornadofx.*

class StatementListView() : SimpleListView<XStatement>() {
	
	constructor(items: ObservableList<XStatement>) : this() {
		this.items = items
	}
	
	constructor(itemsProperty: ObservableValue<out ObservableList<XStatement>>) : this() {
		this.itemsProperty.bind(itemsProperty)
	}
	
	init {
		paddingAll = 8.0
		graphicFactory {
			it.createControl() ?: Label("not supported ${it.javaClass.simpleName}").apply {
				textAlignment = TextAlignment.LEFT
			}
		}
	}
}