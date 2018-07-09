package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.manager
import ej.editor.utils.ContextualTreeSelection
import ej.editor.utils.findItem
import ej.editor.utils.listBinding
import ej.editor.utils.onChangeWeak
import ej.mod.*
import ej.utils.addToList
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.collections.ObservableList
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.input.DataFormat
import javafx.scene.input.Dragboard
import javafx.scene.input.TransferMode
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*

val DATAFORMAT_XSTATEMENT = DataFormat("application/x-ejed-xstatement")
fun Dragboard.hasStatement() = hasContent(DATAFORMAT_XSTATEMENT)

open class StatementTreeWithEditor(val mod:ModData) : VBox() {
	var editor: Region = defaultEditorBody {
		label("<nothing selected>")
		vgrow = Priority.ALWAYS
		hgrow = Priority.ALWAYS
	}
	val tree: StatementTree = StatementTree()
	val splitPane = SplitPane()
	val contentsProperty = tree.contentsProperty
	var contents: ObservableList<XStatement> by contentsProperty
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
		println("[INFO] Removed $me")
		posForInsertionInvalidator.value++
	}
	fun insertStmt(me: XStatement, dest: TreeItem<XStatement>?, destIndex:Int, focus:Boolean) {
		if (dest == null || dest.parent == null) {
			contents.add(destIndex, me)
		} else {
			(dest.value as XComplexStatement).content.add(destIndex, me)
		}
		println("[INFO] Inserted $me")
		posForInsertionInvalidator.value++
		if (focus) focusOnStatement(me)
	}
	fun moveStmt(item: TreeItem<XStatement>, dest: TreeItem<XStatement>?, destIndex: Int,focus:Boolean=true) {
		val wasExpanded = item.isExpanded
		val me = item.value
		removeStmt(item)
		insertStmt(me, dest, destIndex, false)
		if (me != null && focus) focusOnStatement(me, wasExpanded)
	}
	
	fun focusOnStatement(me: XStatement, expand: Boolean = false) {
		tree.findItem { it == me }?.let { item2 ->
			if (expand) item2.expandAll()
			tree.selectionModel.select(item2)
		}
	}
	
	private val posForInsertionInvalidator = SimpleIntegerProperty(0)
	val posForInsertionProperty: ObservableValue<Pair<TreeItem<XStatement>?, Int>?> = contextualCurrentProperty.objectBinding(posForInsertionInvalidator) { cc ->
		posForInsertion(cc)
	}
	fun posForInsertion(cc: ContextualTreeSelection<XStatement>? = contextualCurrent):Pair<TreeItem<XStatement>?,Int> {
		cc ?: return (null to 0)
		val cci = cc.item
		val ccp = cc.parent
		val ccv = cc.value
		if (cci.isLeaf && ccv is XComplexStatement) {
			return cci to 0
		}
		if (cc.inRoot) {
			return null to indexOfStmt(cci)+1
		}
		return ccp to indexOfStmt(cci)+1
	}
	fun insertStmtHere(me: XStatement) {
		val pos = posForInsertion()
		insertStmt(me,pos.first,pos.second,true)
	}
	var wasDragFromTop:Boolean = false
	var dragContent:TreeItem<XStatement>? = null
	private fun setupDrag(cell:TreeCell<XStatement>) {
		cell.setOnDragDetected { event ->
			val db = cell.startDragAndDrop(TransferMode.MOVE)
			dragContent = cell.treeItem
			db.setContent(mapOf(DATAFORMAT_XSTATEMENT to cell.item.toString()))
			event.consume()
		}
		cell.setOnDragOver { event ->
			if (event.gestureSource != cell) {
				if (event.dragboard.hasStatement()) {
					event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
				}
			}
			event.consume()
		}
		cell.setOnDragEntered { event ->
			if (event.gestureSource != cell && event.dragboard.hasStatement()) {
				cell.addClass(Styles.dragover)
				if (event.y < cell.height/2) {
					cell.addClass(Styles.dragoverFromTop)
					wasDragFromTop = true
				} else {
					cell.addClass(Styles.dragoverFromBottom)
					wasDragFromTop = false
				}
			}
			event.consume()
		}
		cell.setOnDragExited { event ->
			cell.removeClass(Styles.dragover,Styles.dragoverFromTop,Styles.dragoverFromBottom)
			event.consume()
		}
		cell.setOnDragDropped { event ->
			val content = dragContent
			if (event.gestureSource != cell && event.dragboard.hasStatement() && content != null) {
				val reference = cell.treeItem
				val target = reference.parent
				if (target != null && generateSequence(reference){it.parent}.none { it == content }) {
					val tgti = target.children.indexOf(reference)
					if (reference.children.isEmpty() && reference.value is XComplexStatement && !wasDragFromTop) {
						moveStmt(content, reference, 0)
					} else {
						moveStmt(content, target, if (wasDragFromTop) tgti else (tgti + 1))
					}
				}
				this.dragContent = null
			}
			event.consume()
		}
	}

	init {
		gridpane {
			hgap = 5.0
			vgap = 5.0
			addClass(Styles.toolbarGrid)
			row {
				label("Options")
				togglebutton {
					expandButton = this
					text = "Expand"
				}
			}
			// Basic and flow control
			row {
				label("Add")
				button("Text").action { insertStmtHere(XcText("Input text here")) }
				button("If") {
					action {
						insertStmtHere(XlIf("").apply {
							content += XlElseIf("")
							content += XlElse()
						})
					}
				}
				button("ElseIf") {
					disableWhen(posForInsertionProperty.booleanBinding { position ->
						// Disable if not inside <if> or has <else> before the insert position
						val target = (position?.first?.value as? XlIf)?.content
						val index = position?.second
						target == null
								|| index == null
								|| target.subList(0, index).any { it is XlElse }
						
					})
					action { insertStmtHere(XlElseIf("")) }
				}
				button("Else") {
					disableWhen(posForInsertionProperty.booleanBinding { position ->
						// Disable if not inside <if>, has <else>, or has <elseif> after insert position
						val target = (position?.first?.value as? XlIf)?.content
						val index = position?.second
						target == null
								|| index == null
								|| target.any { it is XlElse }
								|| target.subList(index, target.size).any { it is XlElseIf }
					})
					action { insertStmtHere(XlElse()) }
				}
				button("Set var").action { insertStmtHere(XsSet().apply {
					inobj = "state"
					varname = mod.stateVars.firstOrNull()?.name?:"flag1"
					value = "1"
				}) }
				button("Display").action { insertStmtHere(XsDisplay()) }
				button("Output").action { insertStmtHere(XsOutput()) }
				button("Comment").action { insertStmtHere(XlComment("")) }
			}
			// Scene-enders
			row {
				label("")
				button("Next") {
					insertStmtHere(XsMenu())
				}
				button("Menu") { isDisable = true }
				button("Button") { isDisable = true }
				button("Forward") { isDisable = true }
				button("Battle").action { insertStmtHere(XsBattle()) }
			}
			// Other actions
			row {
				label("")
				button("DynStats") { isDisable = true }
				button("...") { isDisable = true }
			}
			row {
				label("Edit")
				button("Up") {
					disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
						cts == null || cts.siblings?.firstOrNull()?.equals(cts.item) ?: true
					})
					action {
						val contextualCurrent = contextualCurrent ?: return@action
						moveStmt(contextualCurrent.item, contextualCurrent.parent, indexOfStmt(contextualCurrent.item) - 1)
					}
				}
				button("Down") {
					disableProperty().bind(contextualCurrentProperty.objectBinding { cts ->
						cts == null || cts.siblings?.lastOrNull()?.equals(cts.item) ?: true
					})
					
					action {
						val contextualCurrent = contextualCurrent ?: return@action
						moveStmt(contextualCurrent.item, contextualCurrent.parent, indexOfStmt(contextualCurrent.item) + 1)
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
				cellDecorator = { cell -> setupDrag(cell) }
				expandButton.isSelected = expandedNodes
				expandedNodesProperty.bind(expandButton.selectedProperty())
			}
			contextualCurrentProperty.onChangeWeak { cts ->
				val value = cts?.item?.value
				editor = value?.let { stmt ->
					stmt.manager()?.editorBody(value)
							?: defaultEditorBody { label("TODO ${stmt.javaClass}") }
				} ?: defaultEditorBody { label("<nothing selected>") }
				editor.apply {
					vgrow = Priority.SOMETIMES
					hgrow = Priority.ALWAYS
				}
				splitPane.items[1] = editor
			}.addToList(weakListeners)
			items += editor
			setDividerPosition(0,0.5)
		}
	}
}