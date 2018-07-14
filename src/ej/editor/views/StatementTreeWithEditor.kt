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

	val contextualCurrentProperty = SimpleObjectProperty<ContextualTreeSelection<StatementTreeItem>>().apply {
		bind(tree.selectionModel.selectedItemProperty().select { item ->
					item.parentProperty().select { parent ->
						parent.children.listBinding { ContextualTreeSelection(item) }
					}
				}
		)
	}
	val contextualCurrent: ContextualTreeSelection<StatementTreeItem>? by contextualCurrentProperty

	fun indexOfStmt(item: TreeItem<StatementTreeItem>):Int {
		return item.parent?.children?.indexOf(item)?:contents.indexOf(item.value.stmt)
	}
	fun removeStmt(item: TreeItem<StatementTreeItem>) {
		val me = item.value.stmt
		val target = item.parent?.value?.stmt
		if (target == null) {
			contents.remove(me)
		} else {
			when (target) {
				is XlIf -> when (me) {
					is XlElseIf -> target.elseifGroups.remove(me)
					is XlElse -> target.elseGroup = null
					else -> kotlin.error("Cannot remove $me from $target")
				}
				is XlSwitch -> when(me) {
					is XlSwitchCase -> target.branches.remove(me)
					is XlSwitchDefault -> target.defaultBranch = null
					else -> kotlin.error("Cannot remove $me from $target")
				}
				is XComplexStatement -> target.content.remove(me)
				else -> kotlin.error("Cannot remove $me from $target")
			}
		}
		println("[INFO] Removed $me")
		posForInsertionInvalidator.value++
	}
	fun insertStmt(me: XStatement, dest: TreeItem<StatementTreeItem>?, destIndex:Int, focus:Boolean) {
		if (dest == null || dest.parent == null) {
			contents.add(destIndex, me)
		} else {
			val target = dest.value.stmt
			when (target) {
				is XlIf -> when (me) {
					is XlElseIf -> target.elseifGroups.add(destIndex, me)
					is XlElse -> target.elseGroup = me
					else -> kotlin.error("Cannot insert $me in $target")
				}
				is XlSwitch -> when (me) {
					is XlSwitchCase ->
						target.branches.add(destIndex, me)
					is XlSwitchDefault ->
						target.defaultBranch = me
					else -> kotlin.error("Cannot insert $me in $target")
				}
				is XComplexStatement -> target.content.add(destIndex, me)
				else -> kotlin.error("Cannot insert $me in $target")
			}
		}
		println("[INFO] Inserted $me")
		posForInsertionInvalidator.value++
		if (focus) focusOnStatement(me, true)
	}
	fun canInsert(me: XStatement, dest: TreeItem<StatementTreeItem>?):Boolean {
		val target = dest?.value?.stmt
		return when (target) {
			is XlIf -> me is XlIf
					|| me is PartOfIf
			is XlSwitch -> me is PartOfSwitch
			is XComplexStatement,
			null -> me !is PartOfIf
					&& me !is PartOfSwitch
			else -> false
		}
	}
	fun moveStmt(item: TreeItem<StatementTreeItem>, dest: TreeItem<StatementTreeItem>?, destIndex: Int,focus:Boolean=true) {
		val wasExpanded = item.isExpanded
		val me = item.value.stmt ?: return
		if (!canInsert(me,dest)) {
			println("[WARN] Cannot insert $me into $dest")
			return
		}
		val target = dest?.value?.stmt
		if (target is XlIf) {
			val targetElse = target.elseGroup
			if (me is XlThen) {
				val tmp = ArrayList(me.content)
				me.content.clear()
				target.thenGroup.content.addAll(tmp)
			} else if (me is XlElse && targetElse != null) {
				val tmp = ArrayList(me.content)
				me.content.clear()
				targetElse.content.addAll(tmp)
			} else if (me is XlIf) {
				removeStmt(item)
				target.elseifGroups.add(XlElseIf(me.test).also { elseif ->
					elseif.content.addAll(me.thenGroup.content)
				})
				target.elseifGroups.addAll(me.elseifGroups)
				me.elseGroup?.let { myElse ->
					if (targetElse == null) {
						target.elseGroup = myElse
					} else {
						targetElse.content.addAll(myElse.content)
					}
				}
			} else {
				removeStmt(item)
				insertStmt(me, dest, destIndex, false)
			}
		} else {
			removeStmt(item)
			insertStmt(me, dest, destIndex, false)
		}
		if (focus) focusOnStatement(me, wasExpanded)
	}
	
	fun focusOnStatement(me: XStatement, expand: Boolean = false) {
		tree.findItem { it == me }?.let { item2 ->
			if (expand) item2.expandAll()
			tree.selectionModel.select(item2)
		}
	}
	
	private val posForInsertionInvalidator = SimpleIntegerProperty(0)
	val posForInsertionProperty: ObservableValue<Pair<TreeItem<StatementTreeItem>?, Int>?> = contextualCurrentProperty.objectBinding(posForInsertionInvalidator) { cc ->
		posForInsertion(cc)
	}
	fun posForInsertion(cc: ContextualTreeSelection<StatementTreeItem>? = contextualCurrent):Pair<TreeItem<StatementTreeItem>?,Int> {
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
	var dragContent:TreeItem<StatementTreeItem>? = null
	private fun setupDrag(cell:TreeCell<StatementTreeItem>) {
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
			val content = dragContent ?: return@setOnDragDropped
			val contentStmt = content.value.stmt ?: return@setOnDragDropped
			if (event.gestureSource != cell && event.dragboard.hasStatement()) {
				val reference = cell.treeItem
				val target = reference.parent ?: return@setOnDragDropped
				val targetStmt = target.value.stmt
				println("Dragging over $reference")
				if (generateSequence(reference){it.parent}.none { it == content }) { // not dragging parent inside child
					// Now here things go tricky
					// 1. If target stmt is <if>
					//      a) and content is <else>, <elseif>, <then>, drop into target
					//      b) and content is neither <then>, <else>, <elseif>, drop into reference
					// 2. If target stmt is not <if>
					//      a) and content is neither <then>, <else>, <elseif>:
					//         - if reference is empty complex content and was drag from bottom, drop into reference
					//         - else drop into target
					val dest:TreeItem<StatementTreeItem>
					val destIndex:Int
					val tgti = target.children.indexOf(reference)
					val shift = if (wasDragFromTop) 0 else +1
					// 1.
					if (targetStmt is XlIf) {
						// 1a
						when (contentStmt) {
							is PartOfIf,
							is XlIf -> {
								dest = target
								destIndex = tgti + shift - 1
							}
							else -> {
								dest = reference
								destIndex = 0
							}
						}
					} else {
						if (contentStmt !is PartOfIf) {
							if (reference.children.isEmpty() && reference.value is XComplexStatement && !wasDragFromTop) {
								dest = reference
								destIndex = 0
							} else {
								dest = target
								destIndex = tgti + shift
							}
						} else {
							return@setOnDragDropped
						}
					}
					println("Dropping $content onto $dest . $destIndex")
					moveStmt(content, dest, destIndex)
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
			// Basic
			row {
				label("Add")
				/*
				 * TODO more restrictions:
				 * - <menu>/<next>/<forward><battle>:
				 *   * only in <scene>
				 *   * must be last
				 *   * are mutually exclusive
				 * - <menu>:
				 *   * cannot be nested
				 *   * must have at least one non-disabled button
				 * - <display> only <text>,
				 * - <forward>/<next>/<button> only to <scene>
				 * - No actions allowed in <monster> desc and <button> hint
				 */
				button("Text").action { insertStmtHere(XcText("Input text here")) }
				button("Set var").action { insertStmtHere(XsSet().apply {
					inobj = "state"
					varname = mod.stateVars.firstOrNull()?.name?:"flag1"
					value = "1"
				}) }
				button("Display").action { insertStmtHere(XsDisplay()) }
				button("Output").action { insertStmtHere(XsOutput()) }
				button("Comment").action { insertStmtHere(XlComment("")) }
			}
			// Flow control
			row {
				label("")
				button("If") {
					action {
						insertStmtHere(XlIf().apply {
							elseifGroups.add(XlElseIf())
							elseGroup = XlElse()
						})
					}
				}
				button("ElseIf") {
					disableWhen(posForInsertionProperty.booleanBinding { pos ->
						val stmt = XlElseIf()
						!canInsert(stmt, pos?.first) &&
								!canInsert(stmt, contextualCurrent?.item)
						// Disable if not inside <if>
						//position?.first?.value?.stmt !is XlIf
					})
					action {
						val cc = contextualCurrent
						val pos = posForInsertion()
						val stmt = XlElseIf()
						if (cc != null && canInsert(stmt, cc.item)) {
							insertStmt(stmt,cc.item,0,true)
						} else if (canInsert(stmt, pos.first)) {
							insertStmt(stmt,pos.first,pos.second-1,true)
						}
//						insertStmtHere(XlIf.XlElseIf(""))
					}
				}
				button("Else") {
					disableWhen(posForInsertionProperty.booleanBinding { position ->
						// Disable if not inside <if> w/o <else>
						val target = (position?.first?.value?.stmt as? XlIf)
						target == null || target.elseGroup != null
					})
					action { insertStmtHere(XlElse()) }
				}
				hbox()
				button("Choose") {
					action {
						val stmt = XlSwitch().apply {
							value = "rand(100)"
							branches += XlSwitchCase().apply { lte = "50" }
							defaultBranch = XlSwitchDefault()
						}
						val pos = posForInsertion()
						if (canInsert(stmt,pos.first)) {
							insertStmtHere(stmt)
						}
					}
				}
				button("Branch") {
					action {
						val cc = contextualCurrent ?: return@action
						val stmt = XlSwitchCase().apply { test = "true" }
						if (cc.value?.stmt is XlSwitch) {
							insertStmt(stmt,cc.item,0,true)
						} else if (cc.parent?.value?.stmt is XlSwitch) {
							insertStmt(stmt, cc.parent, indexOfStmt(cc.item), true)
						}
					}
				}
				button("Default") {
					action {
						val cc = contextualCurrent ?: return@action
						val stmt = XlSwitchDefault()
						if (cc.value?.stmt is XlSwitch) {
							insertStmt(stmt,cc.item,0,true)
						} else if (cc.parent?.value?.stmt is XlSwitch) {
							insertStmt(stmt, cc.parent, indexOfStmt(cc.item), true)
						}
					}
				}
			}
			// Scene-enders
			row {
				label("")
				button("Next").action {
					insertStmtHere(XsNext())
				}
				button("Menu").action {
					insertStmtHere(XsMenu().also {
						it.content.add(XsButton("Yes"))
						it.content.add(XsButton("No"))
					})
				}
				button("Button").action {
					disableWhen(posForInsertionProperty.booleanBinding { pfi ->
						generateSequence(pfi?.first) { it.parent }.none { it.value.stmt is XsMenu }
					})
					insertStmtHere(XsButton("Click me"))
				}
				button("Forward") {
					isDisable = true
					action {}
				}
				button("Battle").action { insertStmtHere(XsBattle()) }
			}
			// Other actions
			row {
				label("")
				button("DynStats") {
					isDisable = true
					action {}
				}
				button("...") {
					isDisable = true
					action {}
				}
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
				val value = cts?.item?.value?.stmt
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