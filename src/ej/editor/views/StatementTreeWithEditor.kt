package ej.editor.views

import ej.editor.utils.ContextualTreeSelection
import ej.editor.utils.findItem
import ej.editor.utils.listBinding
import ej.editor.utils.onChangeWeak
import ej.mod.*
import ej.utils.addToList
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.TreeItem
import javafx.scene.layout.*
import tornadofx.*

open class StatementTreeWithEditor : VBox() {
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

	fun indexOfStmt(item: TreeItem<XStatement>):Int {
		val srclist = (item.parent?.value as? XComplexStatement)?.content?:contents
		return srclist.indexOf(item.value)
	}
	fun removeStmt(item: TreeItem<XStatement>) {
		val me = item.value
		val src = item.parent?.value
		if (src == null) {
			contents.remove(me)
		} else {
			(src as XComplexStatement).content.remove(me)
		}
		println("[INFO] Removed $me}") // TODO owner
	}
	fun insertStmt(me: XStatement, dest: TreeItem<XStatement>?, destIndex:Int) {
		if (dest == null) {
			contents.add(destIndex, me)
		} else {
			(dest.value as XComplexStatement).content.add(destIndex, me)
		}
		println("[INFO] Inserted $me") // TODO owner
	}
	fun moveStmt(item: TreeItem<XStatement>, dest: TreeItem<XStatement>?, destIndex: Int) {
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
	fun insertStmtHere(me: XStatement) {
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
				button("If").action { insertStmtHere(XlIf("false")) }
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
					if (value != null) splitPane.items += StmtEditorBodies.bodyFor(value).also {
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