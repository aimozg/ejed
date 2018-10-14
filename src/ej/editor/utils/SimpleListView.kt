package ej.editor.utils

import ej.utils.remove
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Control
import javafx.scene.control.Label
import javafx.scene.control.Skin
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextAlignment
import tornadofx.*

/*
 * Created by aimozg on 24.09.2018.
 * Confidential until published on GitHub
 */
open class SimpleListView<T : Any>() : VBox() {
	val itemsProperty: Property<ObservableList<T>> = object : SimpleObjectProperty<ObservableList<T>>() {
		/*
		override fun invalidated() {
			super.invalidated()
			bindTo(value)
		}
		*/
	}
	var items: ObservableList<T> by itemsProperty
	private val itemsListener: ListChangeListener<T> = ListChangeListener { change ->
		if (items != change.list) return@ListChangeListener
		while (change.next()) {
			val from = change.from
			val to = change.to
			if (change.wasPermutated()) {
				val copy = cells.subList(from, to)
				for (oldIndex in from until to) {
					val newIndex = change.getPermutation(oldIndex)
					copy[newIndex - from] = cells[oldIndex]
				}
				println("Reordering cells $from .. $to")
				cells.subList(from, to).clear()
				cells.addAll(from, copy)
			}
			if (change.wasUpdated()) {
				// do nothing
			}
			if (change.wasRemoved()) {
				val removed = change.removedSize
				println("Removing cells $from .. ${from + removed}")
				cells.remove(from, from + removed)
			}
			if (change.wasAdded()) {
				val added = change.addedSubList.map(::cellFactory)
				println("Adding cells $from .. ${from + added.size}")
				cells.addAll(from, added)
			}
		}
		togglePseudoClass("empty", change.list.isEmpty())
		requestLayout()
	}
	
	protected open fun cellFactory(item: T): SimpleListCell<T> {
		return SimpleListCell(this, item)
	}
	
	var graphicFactory: (SimpleListCell<T>) -> Node = { cell ->
		Label(cell.item.toString()).apply {
			textAlignment = TextAlignment.LEFT
		}
	}
	
	fun graphicFactory(gf: (T) -> Node) {
		graphicFactory = { gf(it.item) }
	}
	
	fun graphicFactory(gf: (SimpleListCell<T>, T) -> Node) {
		graphicFactory = { gf(it, it.item) }
	}
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	open val cells: MutableList<Node> = children
	
	init {
		vgrow = Priority.NEVER
		alignment = Pos.TOP_LEFT
		addClass("simple-list-view")
		
		itemsProperty.addListener { _, old, it ->
			if (old != null) unbindFrom(old)
			if (it != null && scene != null) bindTo(it)
		}
		sceneProperty().onChange {
			unbindFrom(items)
			if (it != null) bindTo(items)
		}
	}
	
	private fun unbindFrom(list: ObservableList<T>?) {
		list?.removeListener(itemsListener)
	}
	
	private fun bindTo(list: ObservableList<T>?) {
		list?.addListener(itemsListener)
		cells.clear()
		cells.addAll(list?.map(::cellFactory) ?: emptyList())
		togglePseudoClass("empty", list?.isEmpty() ?: true)
		requestLayout()
	}
	
	open class SimpleListCell<T : Any>(open val list: SimpleListView<T>, item: T) : Control() {
		val itemProperty = SimpleObjectProperty<T>(item)
		var item: T by itemProperty
		
		init {
			isFocusTraversable = false
			addClass("simple-list-cell")
		}
		override fun getContentBias(): Orientation {
			return Orientation.HORIZONTAL
		}
		
		override fun createDefaultSkin(): Skin<out SimpleListCell<T>> =
				SingleElementSkinBase(
						this,
						NodeBinding(
								itemProperty.objectBinding {
									list.graphicFactory(this)
								}
						)
				)
	}
}