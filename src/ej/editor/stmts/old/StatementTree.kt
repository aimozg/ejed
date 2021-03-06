package ej.editor.stmts.old

import ej.editor.Styles
import ej.editor.utils.findItem
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

fun statementTreeGraphic(tree: StatementTree, stmt: XStatement): Region {
	return stmt.manager()?.treeGraphic(stmt,tree) ?:
	simpleTreeLabel("TODO $stmt").addClass(Styles.xcommand)
}

open class StatementTree : TreeView<XStatement>() {
	val rootStatementProperty = SimpleObjectProperty<XComplexStatement>(XcScene())
	var rootStatement:XComplexStatement by rootStatementProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	var cellDecorator: ((TreeCell<XStatement>)->Unit)? = null
	
	
	fun focusOnStatement(me: XStatement, expand: Boolean = false) {
		findItem { it == me }?.let { item2 ->
			if (expand) {
				item2.parent?.isExpanded = true
				item2.expandAll()
			}
			selectionModel.select(item2)
			runLater { selectionModel.select(item2) }
		}
	}
	
	fun repopulate() {
		populate { treeItem ->
			val sti = treeItem.value
			when (sti) {
				is XlIf -> sti.allGroups
				is XlSwitch -> sti.allGroups
				is XComplexStatement -> sti.content
				else -> emptyList()
			}
		}
	}
	
	init {
		isShowRoot = false
		addClass(Styles.statementTree)
		val tree = this
		cellFormat {
			val cell = this
			cell.addClass(Styles.treeCell)
			cell.prefWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			cell.maxWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			alignment = Pos.TOP_LEFT
			graphic = statementTreeGraphic(tree, it).also { g ->
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

