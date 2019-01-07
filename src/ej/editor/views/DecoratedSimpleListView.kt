package ej.editor.views

import ej.editor.utils.SimpleListView
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.layout.*
import tornadofx.*

/*
 * Created by aimozg on 06.10.2018.
 * Confidential until published on GitHub
 */
open class DecoratedSimpleListView<T : Any>() : SimpleListView<T>() {
	
	val contentBeforeList get() = beforeCells
	val contentAfterList get() = afterCells
	
	var decorateCell: (cell: SimpleListCell<T>, box: Pane, graphic: Node) -> Unit = { _, box, graphic ->
		box += graphic.apply {
			hgrow = Priority.ALWAYS
		}
	}
	
	fun cellDecorator(cd: (cell: SimpleListCell<T>, box: Pane, graphic: Node) -> Unit) {
		decorateCell = cd
	}
	
	override fun addCellGraphic(cell: SimpleListCell<T>, graphic: Node) {
		val box = cellWrapper().apply {
			vgrow = Priority.SOMETIMES
			hgrow = Priority.ALWAYS
		}
		cell.children += box
		decorateCell(cell, box, graphic)
	}
	
	var cellWrapper: () -> Pane = { VBox() }
	
	companion object {
		val CELLWRAPPER_HBOX: () -> Pane = { HBox().apply { alignment = Pos.TOP_LEFT } }
		val CELLWRAPPER_VBOX: () -> Pane = { VBox().apply { alignment = Pos.TOP_LEFT } }
		val CELLWRAPPER_STACK: () -> Pane = { StackPane().apply { alignment = Pos.TOP_LEFT } }
	}
}