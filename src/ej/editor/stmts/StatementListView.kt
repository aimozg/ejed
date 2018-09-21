package ej.editor.stmts

import ej.editor.utils.observableUnique
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.onChangeWeak
import ej.mod.XStatement
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.SkinBase
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import tornadofx.*

class StatementListView(): VBox() {
	val itemsProperty = SimpleObjectProperty<ObservableList<XStatement>>(emptyList<XStatement>().observableUnique())
	var items: ObservableList<XStatement> by itemsProperty
	private var itemsListener: ListChangeListener<XStatement>? = null

	constructor(items: ObservableList<XStatement>): this() {
		this.items = items
	}
	constructor(itemsProperty: ObservableValue<out ObservableList<XStatement>>): this() {
		this.itemsProperty.bind(itemsProperty)
	}
	init {
		/*
		cellFormat {
			val itemGraphic = StmtListItem(itemProperty())
			graphic = itemGraphic
			prefHeight = Control.USE_COMPUTED_SIZE
		}
		*/
		prefHeight = Control.USE_COMPUTED_SIZE
		itemsProperty.onChangeAndNow { list ->
			if (list == null) {
				itemsListener = null
				return@onChangeAndNow
			}
			itemsListener = list.onChangeWeak { change ->
				if (items != change.list) return@onChangeWeak
				while (change.next()) {
					val from = change.from
					val to = change.to
					if (change.wasPermutated()) {
						val copy = children.subList(from,to)
						for (oldIndex in from until to) {
							val newIndex = change.getPermutation(oldIndex)
							copy[newIndex - from] = children[oldIndex]
						}
						children.subList(from,to).clear()
						children.addAll(from,copy)
					}
					if (change.wasUpdated()) {
						// do nothing
					}
					if (change.wasRemoved()) {
						val removed = change.removedSize
						children.remove(from, from+removed)
					}
					if (change.wasAdded()) {
						val added = change.addedSubList.map {
							StmtListItem(it)
						}
						children.addAll(from, added)
					}
				}
			}
			children.clear()
			for (item in list) {
				children += StmtListItem(item)
			}
		}
	}
	class StmtListItem(item: XStatement): Control() {
		val itemProperty = SimpleObjectProperty<XStatement>(item)
		var item: XStatement by itemProperty
		override fun createDefaultSkin() = StmtListItemSkin(this)
	}
	class StmtListItemSkin(control:StmtListItem) : SkinBase<StmtListItem>(control) {
		var main: Node = HBox()
		init {
			control.itemProperty.onChangeAndNow {
				children.remove(main)
				main = control.item.manager()?.listBody(control.item)?: Text("-")
				children.add(main)
			}
		}
	}
}