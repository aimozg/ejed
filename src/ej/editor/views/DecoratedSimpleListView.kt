package ej.editor.views

import ej.editor.utils.SimpleListView
import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.nodeBinding
import ej.editor.utils.nodeBindingNonNull
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Node
import javafx.scene.control.Skin
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 06.10.2018.
 * Confidential until published on GitHub
 */
open class DecoratedSimpleListView<T : Any>() : SimpleListView<T>() {
	
	val beforeListProperty = SimpleObjectProperty<Node?>()
	var beforeList: Node? by beforeListProperty
	
	var nodeBeforeCell: (T) -> Node? = { null }
	fun beforeCell(bc: (T) -> Node?) {
		nodeBeforeCell = bc
	}
	
	var nodeAfterCell: (T) -> Node? = { null }
	fun afterCell(bc: (T) -> Node?) {
		nodeAfterCell = bc
	}
	
	override fun cellFactory(item: T): DecoratedListCell<T> {
		return DecoratedListCell(this, item)
	}
	
	val afterListProperty = SimpleObjectProperty<Node?>()
	var afterList: Node? by afterListProperty
	
	private val cellContainer = VBox()
	override val cells: MutableList<Node> = cellContainer.children
	
	init {
		nodeBinding(beforeListProperty)
		children.add(cellContainer)
		nodeBinding(afterListProperty)
	}
	
	class DecoratedListCell<T : Any>(override val list: DecoratedSimpleListView<T>, item: T) : SimpleListCell<T>(list,
	                                                                                                             item) {
		override fun createDefaultSkin(): Skin<DecoratedListCell<T>> {
			return SingleElementSkinBase<DecoratedListCell<T>>(this, VBox().apply {
				nodeBindingNonNull(itemProperty, list.nodeBeforeCell)
				nodeBindingNonNull(itemProperty, list.graphicFactory)
				nodeBindingNonNull(itemProperty, list.nodeAfterCell)
			})
		}
	}
}