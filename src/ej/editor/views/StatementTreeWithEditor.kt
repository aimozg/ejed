package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.defaultEditorBody
import ej.editor.stmts.manager
import ej.editor.utils.ContextualTreeSelection
import ej.editor.utils.listBinding
import ej.editor.utils.onChangeWeak
import ej.editor.utils.presentWhen
import ej.mod.*
import ej.utils.addToList
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.geometry.Orientation
import javafx.scene.control.SplitPane
import javafx.scene.control.ToggleButton
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.input.DataFormat
import javafx.scene.input.Dragboard
import javafx.scene.input.KeyCode
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
//	val contentsProperty = tree.contentsProperty
//	var contents: ObservableList<XStatement> by contentsProperty
	val rootStatementProperty = tree.rootStatementProperty
		/*SimpleObjectProperty<XComplexStatement>().apply {
		onChange {
			contents = it?.content?: ArrayList<XStatement>().observable()
		}
	}*/
	var rootStatement: XComplexStatement by rootStatementProperty
	val rootAcceptsMenu = rootStatementProperty.booleanBinding { it?.acceptsMenu == true }
	val rootAcceptsActions = rootStatementProperty.booleanBinding { it?.acceptsActions == true }
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
		return item.parent?.children?.indexOf(item)?:rootStatement.content.indexOf(item.value)
	}
	fun canRemoveStmt(item: TreeItem<XStatement>):Boolean {
		val me = item.value
		return me !is XlThen
				&& me !is Encounter.EncounterScene
				&& me !is MonsterData.MonsterDesc
	}
	fun removeStmt(item: TreeItem<XStatement>) {
		val me = item.value
		val target = item.parent?.value
		if (target == null) {
			rootStatement.content.remove(me)
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
	fun insertStmt(me: XStatement, dest: TreeItem<XStatement>?, destIndex:Int, focus:Boolean) {
		if (dest == null || dest.parent == null) {
			rootStatement.content.add(destIndex, me)
		} else {
			val target = dest.value
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
		if (focus) tree.focusOnStatement(me, true)
	}
	fun canInsert(me: XStatement, dest: TreeItem<XStatement>?):Boolean {
		val target = dest?.value
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
	fun moveStmt(item: TreeItem<XStatement>, dest: TreeItem<XStatement>?, destIndex: Int,focus:Boolean=true) {
		val wasExpanded = item.isExpanded
		val me = item.value ?: return
		if (!canInsert(me,dest)) {
			println("[WARN] Cannot insert $me into $dest")
			return
		}
		val target = dest?.value
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
		if (focus) tree.focusOnStatement(me, wasExpanded)
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
			val content = dragContent ?: return@setOnDragDropped
			val contentStmt = content.value ?: return@setOnDragDropped
			if (event.gestureSource != cell && event.dragboard.hasStatement()) {
				val reference = cell.treeItem
				val target = reference.parent ?: return@setOnDragDropped
				val targetStmt = target.value
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
					val dest:TreeItem<XStatement>
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
			/*
			row {
				TODO 'Expanded' mode is broken
				label("Options")
				togglebutton {
					expandButton = this
					text = "Expand"
				}
			}
			*/
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
				button("Text") {
					action { insertStmtHere(XcText("Input text here")) }
				}
				button("Set var") {
					action {
						insertStmtHere(XsSet().apply {
							inobj = "state"
							varname = mod.stateVars.firstOrNull()?.name ?: "flag1"
							value = "1"
						})
					}
				}
				button("Display") {
					action { insertStmtHere(XsDisplay()) }
				}
				button("Output") {
					action { insertStmtHere(XsOutput()) }
				}
				button("Comment") {
					action { insertStmtHere(XlComment("")) }
				}
				hbox()
				// Flow control
				button("If") {
					action {
						insertStmtHere(XlIf().apply {
							elseifGroups.add(XlElseIf())
							elseGroup = XlElse()
						})
					}
				}
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
			}
			// Scene-enders
			row {
				label("").presentWhen(rootAcceptsMenu)
				button("Next") {
					presentWhen(rootAcceptsMenu)
					action {
						insertStmtHere(XsNext())
					}
				}
				button("Menu") {
					presentWhen(rootAcceptsMenu)
					action {
						insertStmtHere(XsMenu().also {
							it.content.add(XsButton("Yes"))
							it.content.add(XsButton("No"))
						})
					}
				}
				button("Button") {
					presentWhen(rootAcceptsMenu)
					disableWhen(posForInsertionProperty.booleanBinding { pfi ->
						generateSequence(pfi?.first) { it.parent }.none { it.value is XsMenu }
					})
					action {
						insertStmtHere(XsButton("Click me"))
					}
				}
				button("Forward") {
					presentWhen(rootAcceptsMenu)
					action { insertStmtHere(XsForward()) }
				}
				button("Battle") {
					presentWhen(rootAcceptsMenu)
					action { insertStmtHere(XsBattle()) }
				}
			}
			// Other actions
			row {
				label("").presentWhen(rootAcceptsActions)
				button("DynStats") {
					isDisable = true
					presentWhen(rootAcceptsActions)
					action {}
				}
				button("...") {
					isDisable = true
					presentWhen(rootAcceptsActions)
					action {}
				}
			}
			// Edit
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
						val item = contextualCurrent?.item ?: return@action
						if (canRemoveStmt(item)) removeStmt(item)
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
				/*
				TODO 'Expanded' mode is broken
				expandButton.isSelected = expandedNodes
				expandedNodesProperty.bind(expandButton.selectedProperty())
				 */
				setOnKeyPressed {
					if (it.code == KeyCode.DELETE
							&& !it.isAltDown && !it.isControlDown && !it.isMetaDown
							&& !it.isShiftDown && !it.isShortcutDown) {
						val item = contextualCurrent?.item
						if (item != null && canRemoveStmt(item)) removeStmt(item)
					}
				}
			}
			contextualCurrentProperty.onChangeWeak { cts ->
				editor = cts?.item?.value?.let { stmt ->
					stmt.manager()?.editorBody(stmt, tree)
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