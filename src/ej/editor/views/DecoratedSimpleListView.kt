package ej.editor.views

import ej.editor.utils.NodeBinding
import ej.editor.utils.SimpleListView
import ej.editor.utils.SingleElementSkinBase
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Skin
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
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
	
	var decorateCell: (cell: DecoratedListCell<T>, box: Pane, graphic: Node) -> Unit = { cell, box, graphic ->
		graphic.apply {
			hgrow = Priority.ALWAYS
		}.attachTo(box)
	}
	
	fun cellDecorator(cd: (cell: DecoratedListCell<T>, box: Pane, graphic: Node) -> Unit) {
		decorateCell = cd
	}
	
	override fun cellFactory(item: T): SimpleListCell<T> {
		return DecoratedListCell(this, item)
	}
	
	val afterListProperty = SimpleObjectProperty<Node?>()
	var afterList: Node? by afterListProperty
	
	init {
		beforeCells += NodeBinding(beforeListProperty)
		afterCells += NodeBinding(afterListProperty)
	}
	
	var cellWrappersOrientation = Orientation.VERTICAL
	
	open class DecoratedListCell<T : Any>(override val list: DecoratedSimpleListView<T>, item: T) : SimpleListCell<T>(
			list,
			item) {
		override fun createDefaultSkin(): Skin<DecoratedListCell<T>> {
			val box = when (list.cellWrappersOrientation) {
				Orientation.HORIZONTAL -> HBox().apply { alignment = Pos.TOP_LEFT }
				Orientation.VERTICAL -> VBox().apply { alignment = Pos.TOP_LEFT }
			}.apply {
				vgrow = Priority.SOMETIMES
				hgrow = Priority.ALWAYS
				val graphic = list.graphicFactory(this@DecoratedListCell)
				list.decorateCell(this@DecoratedListCell, this, graphic)
			}
			return SingleElementSkinBase<DecoratedListCell<T>>(
					this,
					box)
		}
	}
}