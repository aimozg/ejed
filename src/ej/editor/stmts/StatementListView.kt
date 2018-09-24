package ej.editor.stmts

import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.observableUnique
import ej.editor.utils.onChangeAndNow
import ej.editor.utils.onChangeWeak
import ej.mod.ModData
import ej.mod.XStatement
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

class StatementListView(var mod: ModData?) : VBox() {
	val itemsProperty = SimpleObjectProperty<ObservableList<XStatement>>(emptyList<XStatement>().observableUnique())
	var items: ObservableList<XStatement> by itemsProperty
	private var itemsListener: ListChangeListener<XStatement>? = null
	
	constructor(items: ObservableList<XStatement>, mod: ModData?) : this(mod) {
		this.items = items
	}
	
	constructor(itemsProperty: ObservableValue<out ObservableList<XStatement>>, mod: ModData?) : this(mod) {
		this.itemsProperty.bind(itemsProperty)
	}
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	init {
		prefHeight = Control.USE_COMPUTED_SIZE
		vgrow = Priority.SOMETIMES
		isFillWidth = true
		paddingAll = 8.0
		spacing = 4.0
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
							StmtListItem(it, mod)
						}
						children.addAll(from, added)
					}
				}
			}
			children.clear()
			children.addAll(list.map { StmtListItem(it, mod) })
		}
	}
	
	class StmtListItem(item: XStatement, val mod: ModData?) : Control() {
		val itemProperty = SimpleObjectProperty<XStatement>(item)
		var item: XStatement by itemProperty
		override fun getContentBias(): Orientation {
			return Orientation.HORIZONTAL
		}
		
		override fun createDefaultSkin() = StmtListItemSkin(this, mod)
	}
	
	class StmtListItemSkin(control: StmtListItem, mod: ModData?) : SingleElementSkinBase<StmtListItem>(control) {
		override var main: Node = HBox()
		init {
			control.itemProperty.onChangeAndNow {
				children.remove(main)
				val sc = control.item.createControl()
				sc?.mod = mod
				main = sc ?: Label("not supported ${control.item.javaClass.simpleName}").apply {
					textAlignment = TextAlignment.LEFT
				}
				children.add(main)
			}
		}
	}
}