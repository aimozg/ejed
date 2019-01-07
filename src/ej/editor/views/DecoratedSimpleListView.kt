package ej.editor.views

import ej.editor.utils.SimpleListView
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.Node
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
		val box = when (cellWrappersOrientation) {
			Orientation.HORIZONTAL -> HBox().apply { alignment = Pos.TOP_LEFT }
			Orientation.VERTICAL -> VBox().apply { alignment = Pos.TOP_LEFT }
		}.apply {
			vgrow = Priority.SOMETIMES
			hgrow = Priority.ALWAYS
		}
		cell.children += box
		decorateCell(cell, box, graphic)
	}
	
	var cellWrappersOrientation = Orientation.VERTICAL
}