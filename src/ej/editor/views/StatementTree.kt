package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.onChangeWeak
import ej.mod.*
import ej.utils.addToList
import ej.utils.affixNonEmpty
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeGraphic(tree:StatementTree, stmt: XStatement): Region {
	return when (stmt) {
		is XlIf -> Label().apply{
			textProperty().bind(
					stmt.testProperty().stringBinding { "If: $it" }
			)
			addClass(Styles.xlogic)
		}
		is XlElse -> Label("Else:").addClass(Styles.xlogic)
		is XlElseIf -> Label("Else if: ${stmt.test}").addClass(Styles.xlogic)
		is XcStyledText -> TextNodeLabel(tree,stmt)

		is XsOutput -> Label("Output: ${stmt.expression.squeezeWs()}").addClass(Styles.xcommand)
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
		is XsDisplay -> Label("Display: ${stmt.ref}").addClass(Styles.xcommand)
		is XsBattle ->
			Label(
					"Battle ${stmt.monster}" + (stmt.options.affixNonEmpty(" with options: "))
			).addClass(Styles.xcommand)
		
		is XcLib -> Label("<lib ${stmt.name}>").addClass(Styles.xcomment)
		is XcNamedText -> Label("<text ${stmt.name}>").addClass(Styles.xcomment)

		else -> Label("<Unknown/TODO> " + stmt.toSourceString().squeezeWs()).addClass(Styles.xcommand)
	}
}

open class StatementTree : TreeView<XStatement>() {
	val contentsProperty = SimpleObjectProperty(ArrayList<XStatement>().observable())
	var contents by contentsProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	private val fakeRoot = TreeItem<XStatement>()
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
			this.prefWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			this.maxWidthProperty().bind(tree.widthProperty().minus(16)) // vscrollbar
			alignment = Pos.TOP_LEFT
			graphic = statementTreeGraphic(tree,it).also { g ->
				g.addClass(Styles.treeNode)
				g.maxWidthProperty().bind(
					cell.maxWidthProperty()
							.doubleBinding(g.layoutXProperty()) { cellMaxWidth ->
								(cellMaxWidth?.toDouble()?:0.0) -
										g.layoutX -
										cell.paddingHorizontal.toDouble()
							}
				)
			}
		}
		
		contentsProperty.onChange {
			repopulate()
		}
	}
}

open class XStatementTreeWithEditor : VBox() {
	var editor: Region = Pane()
	val tree: StatementTree = StatementTree()
	val splitPane = SplitPane()
	val contentsProperty = tree.contentsProperty
	var contents by contentsProperty
	private var expandButton by singleAssign<ToggleButton>()
	private val weakListeners = ArrayList<Any>()
	
	init {
		hbox {
			togglebutton {
				expandButton = this
				text = "Expand"
			}
		}
		splitPane.attachTo(this) {
			orientation = Orientation.VERTICAL
			vgrow = Priority.ALWAYS
			items += tree.apply {
				vgrow = Priority.SOMETIMES
				selectionModel.selectedItemProperty().onChangeWeak { treeItem ->
					splitPane.items -= editor
					val value = treeItem?.value
					if (value != null) splitPane.items +=  StmtEditorBody.bodyFor(value).also { editor = it }
				}.addToList(weakListeners)
				expandButton.isSelected = expandedNodes
				expandedNodesProperty.bind(expandButton.selectedProperty())
			}
		}
		
	}
}