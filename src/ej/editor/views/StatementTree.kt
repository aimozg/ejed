package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.manager
import ej.editor.stmts.simpleTreeLabel
import ej.editor.utils.*
import ej.mod.*
import ej.utils.affixNonEmpty
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
	return when (stmt) {

		is XsBattle -> simpleTreeLabel(
				binding2(stmt.monsterProperty,stmt.optionsProperty) { monster, options ->
					"Battle $monster" + (options.affixNonEmpty(" with options: "))
				}
		).addClass(Styles.xcommand)
		
		is XcLib -> simpleTreeLabel(
				binding1(stmt.nameProperty()){ "<lib $it>" }
		).addClass(Styles.xcomment)
		is XcNamedText -> simpleTreeLabel(
				binding1(stmt.nameProperty()){ "<text $it>" }
		).addClass(Styles.xcomment)

		else -> stmt.manager()?.treeGraphic(stmt,tree) ?:
				simpleTreeLabel("TODO $stmt").addClass(Styles.xcommand)
	}
}

sealed class StatementTreeItem(open val stmt:XStatement?) {
	override fun toString(): String {
		return stmt?.toString()?:javaClass.simpleName
	}
	
	open fun statementTreeGraphic(tree:StatementTree):Region =
			stmt?.let { stmt -> statementTreeGraphic(tree,stmt)} ?:
			simpleTreeLabel("<nothing>")
	class Statement(override val stmt:XStatement): StatementTreeItem(stmt)
	class RootItem: StatementTreeItem(null) {
		override fun statementTreeGraphic(tree: StatementTree) = simpleTreeLabel("<root>")
	}
	abstract class ContentGroupNode(override val stmt:XContentContainer):StatementTreeItem(stmt)
	class ThenNode(val parent:XlIf, override val stmt: XlThen):ContentGroupNode(stmt)
	class ElseIfNode(val parent:XlIf, override val stmt: XlElseIf):ContentGroupNode(stmt)
	class ElseNode(val parent:XlIf, override val stmt: XlElse):ContentGroupNode(stmt)
}

open class StatementTree : TreeView<StatementTreeItem>() {
	val contentsProperty = SimpleObjectProperty(ArrayList<XStatement>().observableUnique())
	var contents by contentsProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	var cellDecorator: ((TreeCell<StatementTreeItem>)->Unit)? = null
	fun repopulate() {
		populate { treeItem ->
			val sti = treeItem.value
			when (sti) {
				null -> emptyList()
				is StatementTreeItem.RootItem -> contents.transformed {
					StatementTreeItem.Statement(it)
				}
				is StatementTreeItem.Statement -> when (sti.stmt) {
					is XlIf ->
						observableConcatenation(
								listOf(
										StatementTreeItem.ThenNode(sti.stmt, sti.stmt.thenGroup)
								).observable(),
								sti.stmt.elseifGroups.transformed { e ->
									StatementTreeItem.ElseIfNode(sti.stmt, e)
								},
								ObservableSingletonList(sti.stmt.elseGroupProperty).transformed { e ->
									StatementTreeItem.ElseNode(sti.stmt, e)
								}
						)
					is XContentContainer -> sti.stmt.content.transformed {
						StatementTreeItem.Statement(it)
					}
					else -> emptyList()
				}
				is StatementTreeItem.ContentGroupNode -> sti.stmt.content.transformed {
					StatementTreeItem.Statement(it)
				}
			}
		}
	}
	
	init {
		isShowRoot = false
		root = TreeItem(StatementTreeItem.RootItem())
		val tree = this
		cellFormat {
			val cell = this
			cell.addClass(Styles.treeCell)
			cell.prefWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			cell.maxWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			alignment = Pos.TOP_LEFT
			graphic = it.statementTreeGraphic(tree).also { g ->
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
		
		contentsProperty.onChange {
			repopulate()
		}
	}
}

