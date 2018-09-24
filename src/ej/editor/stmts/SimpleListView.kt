package ej.editor.stmts

import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.observableUnique
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.onChangeWeak
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import tornadofx.*

/*
 * Created by aimozg on 24.09.2018.
 * Confidential until published on GitHub
 */
open class SimpleListView<T : Any>() : VBox() {
	val itemsProperty = SimpleObjectProperty<ObservableList<T>>(emptyList<T>().observableUnique())
	var items: ObservableList<T> by itemsProperty
	private var itemsListener: ListChangeListener<T>? = null
	
	var cellFactory: (T) -> SimpleListCell<T> = { item ->
		SimpleListCell(this, item)
	}
	var graphicFactory: (T) -> Node = { item ->
		Label(item.toString()).apply {
			textAlignment = TextAlignment.LEFT
		}
	}
	
	fun graphicFactory(gf: (T) -> Node) {
		graphicFactory = gf
	}
	
	fun createGraphic(item: T?): Node {
		if (item != null) return graphicFactory(item)
		return HBox()
	}
	
	constructor(items: ObservableList<T>) : this() {
		this.items = items
	}
	
	constructor(itemsProperty: ObservableValue<out ObservableList<T>>) : this() {
		this.itemsProperty.bind(itemsProperty)
	}
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	init {
		vgrow = Priority.SOMETIMES
		
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
						val copy = children.subList(from, to)
						for (oldIndex in from until to) {
							val newIndex = change.getPermutation(oldIndex)
							copy[newIndex - from] = children[oldIndex]
						}
						children.subList(from, to).clear()
						children.addAll(from, copy)
					}
					if (change.wasUpdated()) {
						// do nothing
					}
					if (change.wasRemoved()) {
						val removed = change.removedSize
						children.remove(from, from + removed)
					}
					if (change.wasAdded()) {
						val added = change.addedSubList.map(cellFactory)
						children.addAll(from, added)
					}
				}
			}
			children.clear()
			children.addAll(list.map(cellFactory))
		}
	}
	
	class SimpleListCell<T : Any>(val list: SimpleListView<T>, item: T) : Control() {
		val itemProperty = SimpleObjectProperty<T>(item)
		var item: T by itemProperty
		override fun getContentBias(): Orientation {
			return Orientation.HORIZONTAL
		}
		
		override fun createDefaultSkin() = SimpleListCellSkin(this)
	}
	
	class SimpleListCellSkin<T : Any>(control: SimpleListCell<T>) : SingleElementSkinBase<SimpleListCell<T>>(control) {
		override var main: Node = control.list.createGraphic(control.item)
		
		init {
			control.itemProperty.onChangeAndNow {
				children.remove(main)
				main = skinnable.list.createGraphic(it)
				children.add(main)
			}
		}
	}
}