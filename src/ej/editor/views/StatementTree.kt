package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.StmtEditorLabel
import ej.editor.stmts.manager
import ej.mod.*
import ej.utils.affixNonEmpty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
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
		is XlElse -> Label("Else:").addClass(Styles.xlogic)
		is XlElseIf -> Label("Else if: ${stmt.test}").addClass(Styles.xlogic)
		is XcText -> TextNodeLabel(tree, stmt)

		is XsSet -> Label().apply{
			addClass(Styles.xcommand)
			val s:String = if (stmt.inobj != null) {
				"property '${stmt.varname}' of ${stmt.inobj}"
			} else {
				"variable '${stmt.varname}'"
			}
			text = when (stmt.op) {
				"add", "+", "+=" -> "Add ${stmt.value} to $s"
				null, "set", "=" -> "Set ${stmt.value} to $s"
				else -> "Apply ${stmt.op}${stmt.value} to $s"
			}
		}
		is XsBattle ->
			Label(
					"Battle ${stmt.monster}" + (stmt.options.affixNonEmpty(" with options: "))
			).addClass(Styles.xcommand)
		
		is XcLib -> Label().apply {
			textProperty().bind(stmt.nameProperty().stringBinding{ "<lib $it>" })
			addClass(Styles.xcomment)
		}
		is XcNamedText -> Label().apply {
			textProperty().bind(stmt.nameProperty().stringBinding{ "<text $it>" })
			addClass(Styles.xcomment)
		}

		else -> stmt.manager()?.treeGraphic(stmt,tree) ?:
				StmtEditorLabel(stmt) {
					label("TODO $stmt").addClass(Styles.xcommand)
				}
	}
}

open class StatementTree : TreeView<XStatement>() {
	val contentsProperty = SimpleObjectProperty(ArrayList<XStatement>().observable())
	var contents by contentsProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	private val fakeRoot = TreeItem<XStatement>()
	var cellDecorator: ((TreeCell<XStatement>)->Unit)? = null
	fun repopulate() {
		populate {
			val stmt = it.value
			when (stmt) {
				null -> if (it == fakeRoot) contents else emptyList()
				is XcLib, is XcNamedText -> emptyList()
				is XContentContainer -> stmt.content
				else -> emptyList()
			}
		}
	}
	
	init {
		isShowRoot = false
		root = fakeRoot
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
		
		contentsProperty.onChange {
			repopulate()
		}
	}
}

