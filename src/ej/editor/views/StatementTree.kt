package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.ContextualTreeSelection
import ej.editor.utils.findItem
import ej.editor.utils.listBinding
import ej.editor.utils.onChangeWeak
import ej.mod.*
import ej.utils.addToList
import ej.utils.affixNonEmpty
import ej.utils.squeezeWs
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.scene.control.*
import javafx.scene.layout.*
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

open class XStatementTreeWithEditor : VBox() {
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

	fun indexOfStmt(item:TreeItem<XStatement>):Int {
		val srclist = (item.parent?.value as? XComplexStatement)?.content?:contents
		return srclist.indexOf(item.value)
	}
	fun removeStmt(item:TreeItem<XStatement>) {
		val me = item.value
		val src = item.parent?.value
		if (src == null) {
			contents.remove(me)
		} else {
			(src as XComplexStatement).content.remove(me)
		}
		println("[INFO] Removed $me}") // TODO owner
	}
	fun insertStmt(me:XStatement, dest:TreeItem<XStatement>?, destIndex:Int) {
		if (dest == null) {
			contents.add(destIndex, me)
		} else {
			(dest.value as XComplexStatement).content.add(destIndex, me)
		}
		println("[INFO] Inserted $me") // TODO owner
	}
	fun moveStmt(item:TreeItem<XStatement>, dest:TreeItem<XStatement>?, destIndex: Int) {
		val wasExpanded = item.isExpanded
		val me = item.value
		removeStmt(item)
		insertStmt(me, dest, destIndex)
		tree.findItem { it == me }?.let { item2 ->
			if (wasExpanded) item2.expandAll()
			tree.selectionModel.select(item2)
		}
	}
	fun posForInsertion():Pair<TreeItem<XStatement>?,Int> {
		val cc = contextualCurrent ?: return (null to 0)
		val cci = cc.item
		val ccp = cc.parent
		val ccv = cc.value
		if (cci.isLeaf && ccv is XComplexStatement) {
			return cci to 0
		}
		if (cc.inRoot) {
			return null to indexOfStmt(cci)
		}
		return ccp to indexOfStmt(cci)
	}
	fun insertStmtHere(me:XStatement) {
		val pos = posForInsertion()
		insertStmt(me,pos.first,pos.second)
	}

	init {
		gridpane {
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
				button("Text").action { insertStmtHere(XcText("Input text here")) }
				button("If-Else") { isDisable = true }
				button("Display").action { insertStmtHere(XsDisplay()) }
				button("Output").action { insertStmtHere(XsOutput()) }
				button("Battle").action { insertStmtHere(XsBattle()) }
				button("...") { isDisable = true }
			}
			row {
				label("Edit")
				button("Move Up") {
					disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
						cts == null || cts.siblings?.firstOrNull()?.equals(cts.item) ?: true
					})
					action {
						val contextualCurrent = contextualCurrent ?: return@action
						moveStmt(contextualCurrent.item, null, indexOfStmt(contextualCurrent.item) - 1)
					}
				}
				button("Move Down") {
					disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
						cts == null || cts.siblings?.lastOrNull()?.equals(cts.item) ?: true
					})
					
					action {
						val contextualCurrent = contextualCurrent ?: return@action
						moveStmt(contextualCurrent.item, null, indexOfStmt(contextualCurrent.item) + 1)
					}
				}
				button("Remove") {
					disableProperty().bind(contextualCurrentProperty.isNull)
					action {
						removeStmt(contextualCurrent?.item ?: return@action)
					}
				}
			}
		}
		splitPane.attachTo(this) {
			orientation = Orientation.VERTICAL
			gridpaneConstraints { columnSpan = GridPane.REMAINING }
			vgrow = Priority.ALWAYS
			hgrow = Priority.ALWAYS
			items += tree.apply {
				vgrow = Priority.SOMETIMES
				hgrow = Priority.ALWAYS
				contextualCurrentProperty.onChangeWeak { cts ->
					splitPane.items -= editor
					val value = cts?.item?.value
					if (value != null) splitPane.items += StmtEditorBody.bodyFor(value).also {
						vgrow = Priority.SOMETIMES
						hgrow = Priority.ALWAYS
						editor = it
					}
				}.addToList(weakListeners)
				expandButton.isSelected = expandedNodes
				expandedNodesProperty.bind(expandButton.selectedProperty())
			}
			items += editor
		}
	}
}