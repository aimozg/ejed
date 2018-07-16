package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.manager
import ej.editor.stmts.simpleTreeLabel
import ej.editor.utils.ObservableSingletonList
import ej.editor.utils.observableConcatenation
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Region
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeGraphic(tree:StatementTree, stmt: XStatement): Region {
	return stmt.manager()?.treeGraphic(stmt,tree) ?:
	simpleTreeLabel("TODO $stmt").addClass(Styles.xcommand)
}

open class StatementTree : TreeView<XStatement>() {
	val rootStatementProperty = SimpleObjectProperty<XComplexStatement>(XcScene())
	var rootStatement:XComplexStatement by rootStatementProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	var cellDecorator: ((TreeCell<XStatement>)->Unit)? = null
	fun repopulate() {
		populate { treeItem ->
			val sti = treeItem.value
			when (sti) {
				is XlIf ->
					observableConcatenation(
							listOf(sti.thenGroup).observable(),
							sti.elseifGroups,
							ObservableSingletonList(sti.elseGroupProperty)
					)
				is XlSwitch ->
					observableConcatenation(
							sti.branches,
							ObservableSingletonList(sti.defaultBranchProperty)
					)
				is XComplexStatement -> sti.content
				else -> emptyList()
			}
		}
	}
	
	init {
		isShowRoot = false
		val tree = this
		cellFormat {
			val cell = this
			cell.addClass(Styles.treeCell)
			cell.prefWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			cell.maxWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			alignment = Pos.TOP_LEFT
			graphic = statementTreeGraphic(tree,it).also { g ->
				g.addClass(Styles.treeGraphic)
				g.maxWidthProperty().bind(
					cell.maxWidthProperty()
							.doubleBinding(g.layoutXProperty()) { cellMaxWidth ->
								(cellMaxWidth?.toDouble()?:0.0) -
										g.layoutX -
										cell.paddingHorizontal.toDouble()
							}
				)
			}
			cellDecorator?.invoke(cell)
		}
		
		rootStatementProperty.onChange {
			root = TreeItem(it)
			repopulate()
		}
	}
}

