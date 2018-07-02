package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.onChangeAndNow
import ej.mod.*
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
import javafx.scene.text.TextFlow
import tornadofx.*

/*
 * Created by aimozg on 01.07.2018.
 * Confidential until published on GitHub
 */

fun statementTreeGraphic(tree:StatementTreeView, stmt: XStatement): Region {
	return when (stmt) {
		is XlIf -> Label("If: ${stmt.test}").addClass(Styles.xlogic)
		is XlElse -> Label("Else:").addClass(Styles.xlogic)
		is XlElseIf -> Label("Else if: ${stmt.test}").addClass(Styles.xlogic)
//		is XcTextNode -> Label(stmt.content).addClass(Styles.xtext)
		is XcStyledText -> VBox().apply {
			val g = this
			val flow = TextFlow().apply {
				prefWidthProperty().bind(g.widthProperty())
				maxWidthProperty().bind(g.widthProperty())
//				prefWidth = Region.USE_COMPUTED_SIZE
//				minWidth = Region.USE_COMPUTED_SIZE
				for (run in stmt.runs) {
					text(run.content) {
						style = run.style.toCss()
						addClass(Styles.xtext)
					}
				}
			}
			val label = Label(stmt.textContent.replace("\n"," ")).addClass(Styles.xtext)
			tree.expandedNodesProperty.onChangeAndNow {
				if (it == true) {
					label.removeFromParent()
					flow.attachTo(this)
				} else {
					flow.removeFromParent()
					label.attachTo(this)
				}
			}
		}
		

		is XsOutput -> Label("Output: ${stmt.expression.squeezeWs()}").addClass(Styles.xcommand)
		is XsDisplay -> Label("Display: ${stmt.ref}").addClass(Styles.xcommand)
		is XsBattle ->
			Label(
					"Battle ${stmt.monster}" + (stmt.options.affixNonEmpty(" with options: "))
			).addClass(Styles.xcommand)
		
		is XcLib -> Label("<lib ${stmt.name}>").addClass(Styles.xcomment)

		else -> Label("<Unknown/TODO> " + stmt.toSourceString().squeezeWs()).addClass(Styles.xcommand)
	}
}

open class StatementTreeView : TreeView<XStatement>() {
	val contentsProperty = SimpleObjectProperty<MutableList<XStatement>>(ArrayList())
	var contents by contentsProperty
	
	val expandedNodesProperty = SimpleObjectProperty<Boolean>(false)
	var expandedNodes by expandedNodesProperty
	
	private val fakeRoot = TreeItem<XStatement>()
	fun repopulate() {
		populate {
			val stmt = it.value
			when (stmt) {
				null -> if (it == fakeRoot) contents else emptyList()
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
		/*
		expandedNodesProperty.onChange {
			if (it == true) addClass(Styles.treeExpanded) else removeClass(Styles.treeExpanded)
		}*/
	}
}

open class XStatementTreeWithEditor : VBox() {
	var editor: Region = Pane()
	val tree: StatementTreeView = StatementTreeView()
	val splitPane = SplitPane()
	val contentsProperty = tree.contentsProperty
	var contents by contentsProperty
	private var expandButton by singleAssign<ToggleButton>()
	
	init {
		hbox {
			togglebutton {
				expandButton = this
				text = "Expand"
			}
		}
		this += splitPane.apply {
			orientation = Orientation.VERTICAL
			vgrow = Priority.ALWAYS
			items += tree.apply {
				vgrow = Priority.SOMETIMES
				selectionModel.selectedItemProperty().onChange { treeItem ->
					splitPane.items -= editor
					val value = treeItem?.value
					if (value != null) splitPane.items +=  StmtEditorBody.bodyFor(value).also { editor = it }
				}
				expandButton.isSelected = expandedNodes
				expandedNodesProperty.bind(expandButton.selectedProperty())
			}
		}
		
	}
}