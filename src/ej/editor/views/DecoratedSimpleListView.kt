package ej.editor.views

import ej.editor.utils.NodeBinding
import ej.editor.utils.SimpleListView
import ej.editor.utils.SingleElementSkinBase
import ej.editor.utils.nodeBindingNonNull
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Skin
import javafx.scene.layout.Priority
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
	
	init {
		beforeCells += NodeBinding(beforeListProperty)
		afterCells += NodeBinding(afterListProperty)
	}
	
	class DecoratedListCell<T : Any>(override val list: DecoratedSimpleListView<T>, item: T) : SimpleListCell<T>(list,
	                                                                                                             item) {
		override fun createDefaultSkin(): Skin<DecoratedListCell<T>> {
			return SingleElementSkinBase<DecoratedListCell<T>>(this, VBox().apply {
				vgrow = Priority.SOMETIMES
				alignment = Pos.TOP_LEFT
				nodeBindingNonNull(itemProperty, list.nodeBeforeCell)
				nodeBindingNonNull(itemProperty) { list.graphicFactory(this@DecoratedListCell) }
				nodeBindingNonNull(itemProperty, list.nodeAfterCell)
			})
		}
	}
}