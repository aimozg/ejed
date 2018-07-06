package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.ContextualTreeSelection
import ej.editor.utils.listBinding
import ej.editor.utils.onChangeWeak
import ej.editor.utils.select
import ej.mod.*
import ej.utils.addToList
import ej.utils.affixNonEmpty
import ej.utils.longSwap
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
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
		is XcText -> TextNodeLabel(tree, stmt)

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
		
		is XcLib -> Label().apply {
			textProperty().bind(stmt.nameProperty().stringBinding{ "<lib $it>" })
			addClass(Styles.xcomment)
		}
		is XcNamedText -> Label().apply {
			textProperty().bind(stmt.nameProperty().stringBinding{ "<text $it>" })
			addClass(Styles.xcomment)
		}

		else -> Label("TODO $stmt").addClass(Styles.xcommand)
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

open class XStatementTreeWithEditor : GridPane() {
	var editor: Region = Pane()
	val tree: StatementTree = StatementTree()
	val splitPane = SplitPane()
	val contentsProperty = tree.contentsProperty
	var contents by contentsProperty
	private var expandButton by singleAssign<ToggleButton>()
	private val weakListeners = ArrayList<Any>()

	val contextualCurrentProperty = SimpleObjectProperty<ContextualTreeSelection<XStatement>>().apply {
		bind(tree.selectionModel.selectedItemProperty().select { item ->
					item.parentProperty().select { parent ->
						parent.children.listBinding { ContextualTreeSelection(item) }
					}
				}
		)
	}
	val contextualCurrent: ContextualTreeSelection<XStatement>? by contextualCurrentProperty
	val selected:XStatement? get() = contextualCurrentProperty.value?.value
	init {
		hgap = 5.0
		vgap = 5.0
		row {
			label("Options")
			togglebutton {
				expandButton = this
				text = "Expand"
			}
		}
		row {
			label("Add")
			button("Text")
			button("If-Else") { isDisable = true }
			button("Display") { isDisable = true }
			button("Output") { isDisable = true }
			button("Battle") { isDisable = true }
			button("...") { isDisable = true }
		}
		row {
			label("Edit")
			button("Move Up") {
				disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
					cts == null || cts.siblings?.firstOrNull()?.equals(cts.item)?:true
				})
				action {
					val contextualCurrent = contextualCurrent ?: return@action
					val (item, parent, siblings) = contextualCurrent
					if (parent == null || siblings == null || parent.value == null) {
						val i = contents.indexOf(item.value)
						if (contents.longSwap(i, i-1)) {
							println("[INFO] Moved up $item") // TODO owner
							tree.select { it == item.value }
						} else {
							println("[WARN] Cannot move up $item")
						}
					} else {
						val i = siblings.indexOf(item)
						if (parent.value?.swap(i, i-1) == true) {
							println("[INFO] Moved up $item") // TODO owner
							tree.select { it == item.value }
						} else {
							println("[WARN] Cannot move up $item")
						}
					}
				}
			}
			button("Move Down") {
				disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
					cts == null || cts.siblings?.lastOrNull()?.equals(cts.item)?:true
				})
				
				action {
					val selected = selected
					when (selected) {
						// todo
					}
				}
			}
			button("Remove") {
				disableProperty().bind(contextualCurrentProperty.isNull)
				action {
					val parent = contextualCurrent?.parentValue
					val item = contextualCurrent?.value ?: return@action
					if (parent == null) {
						if (contents.remove(item)) {
							println("[INFO] Removed $item") // TODO owner
						} else {
							println("[WARN] Cannot remove $item")
						}
					} else {
						if (parent.remove(item)) {
							println("[INFO] Removed $item from $parent")
						} else {
							println("[WARN] Cannot remove $item from $parent")
						}
					}
				}
			}
		}
		row {
			splitPane.attachTo(this) {
				orientation = Orientation.VERTICAL
				gridpaneConstraints { columnSpan = GridPane.REMAINING }
				vgrow = Priority.ALWAYS
				items += tree.apply {
					vgrow = Priority.SOMETIMES
					contextualCurrentProperty.onChangeWeak { cts ->
						splitPane.items -= editor
						val value = cts?.item?.value
						if (value != null) splitPane.items += StmtEditorBody.bodyFor(value).also { editor = it }
					}.addToList(weakListeners)
					expandButton.isSelected = expandedNodes
					expandedNodesProperty.bind(expandButton.selectedProperty())
				}
			}
		}
	}
}