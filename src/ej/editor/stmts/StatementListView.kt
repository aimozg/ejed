package ej.editor.stmts

import ej.editor.Styles
import ej.editor.stmts.old.DATAFORMAT_XSTATEMENT
import ej.editor.stmts.old.hasStatement
import ej.editor.utils.*
import ej.editor.views.DecoratedSimpleListView
import ej.mod.XStatement
import ej.mod.XStatementFromXmlObject
import ej.mod.toXmlObject
import ej.utils.addAfter
import ej.utils.addBefore
import ej.xml.XmllikeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Orientation
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.input.KeyCombination
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*

class StatementListView : DecoratedSimpleListView<XStatement>(), ContextMenuContainer {
	
	val expandedProperty = SimpleBooleanProperty(true)
	var expanded by expandedProperty
	
	override val menus by lazy {
		listOf(Menu("_List").apply {
			menu("Collapse/E_xpand") {
				action {
					expanded = !expanded
				}
			}
			menu("Insert At _Start") {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertAfter(null, e.factory(), true)
						}
					}
				}
			}
			menu("Insert At _End") {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertAfter(null, e.factory(), true)
						}
					}
				}
			}
		})
	}
	val expandButton: Button by lazy {
		Button().apply {
			addClass("small-button")
			isFocusTraversable = false
			graphic = boundFaGlyph(expandedProperty.stringBinding { expanded ->
				if (expanded == true) FontAwesome.Glyph.CARET_DOWN.char.toString()
				else FontAwesome.Glyph.CARET_RIGHT.char.toString()
			})
			action {
				expanded = !expanded
			}
		}
	}
	val insertFirstButton by lazy {
		Button().apply {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.PLUS)
			contextmenu {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertAfter(null, e.factory(), true)
						}
					}
				}
			}
			action {
				contextMenu.show(this, Side.BOTTOM, 0.0, 0.0)
			}
		}
	}
	val insertLastButton by lazy {
		Button().apply {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.PLUS)
			contextmenu {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertBefore(null, e.factory(), true)
						}
					}
				}
			}
			action {
				contextMenu.show(this, Side.BOTTOM, 0.0, 0.0)
			}
		}
	}
	val listTopMenu by lazy {
		HBox().apply {
			addClass("stmt-ctrl-listmenu")
			this += expandButton
			this += insertFirstButton
		}
	}
	val listBottomMenu by lazy {
		HBox().apply {
			addClass("stmt-ctrl-listmenu")
			this += insertLastButton
		}
	}
	
	fun deleteStmt(stmt: XStatement) {
		println("Deleting $stmt")
		items.remove(stmt)
	}
	
	fun insertAfter(ref: XStatement?, stmt: XStatement, andFocus: Boolean) {
		println("Inserting $stmt after $ref")
		items.addAfter(ref, stmt)
		if (andFocus) (cells.getOrNull(items.indexOf(stmt)) as? StatementCell)?.requestFocus()
	}
	
	fun insertBefore(ref: XStatement?, stmt: XStatement, andFocus: Boolean) {
		println("Inserting $stmt before $ref")
		items.addBefore(ref, stmt)
		if (andFocus) (cells.getOrNull(items.indexOf(stmt)) as? StatementCell)?.requestFocus()
	}
	
	private var wasDragFromTop = false
	override fun createCellInstance(item: XStatement): SimpleListCell<XStatement> {
		return StatementCell(this, item)
	}
	
	init {
		cellWrappersOrientation = Orientation.HORIZONTAL
		graphicFactory { stmt ->
			(stmt.createControl() ?: Label("not supported ${stmt.javaClass.simpleName}").apply {
				// contextMenu = cellMenu
				textAlignment = TextAlignment.LEFT
			}).apply {
				hgrow = Priority.ALWAYS
			}
		}
		cellDecorator { cell, box, graphic ->
			configureDragAndDrop(cell)
			box += graphic
			box += StackPane().apply {
				addClass("stmt-ctrl-itemmenu")
				alignment = Pos.TOP_LEFT
				minWidth = Region.USE_PREF_SIZE
				minHeight = Region.USE_PREF_SIZE
				maxWidth = Region.USE_PREF_SIZE
				maxHeight = Region.USE_PREF_SIZE
				button().apply {
					addClass("small-button")
					this.graphic = fontAwesome.create(FontAwesome.Glyph.REORDER)
					isFocusTraversable = false
					makeDragAndDropStarter(this, cell, cell.item)
				}
			}
		}
		
		cells.onChange {
			while (it.next()) {
				for (cell in it.addedSubList) cell.presentWhen(expandedProperty)
			}
		}
	}
	
	private fun makeDragAndDropStarter(btn: Node,
	                                   cell: SimpleListCell<XStatement>,
	                                   stmt: XStatement) {
		btn.setOnDragDetected { event ->
			val db = cell.startDragAndDrop(TransferMode.MOVE)
			val content = stmt.toXmlObject()
			//							println("Dragging $content")
			db.setContent(mapOf(DATAFORMAT_XSTATEMENT to content.toBytes()))
			cell.addClass(Styles.dragged)
			event.consume()
			cell.setOnDragDone { done ->
				cell.removeClass(Styles.dragged)
				if (done.isAccepted) {
					items.remove(cell.item)
				}
				done.consume()
			}
		}
	}
	
	private fun configureDragAndDrop(cell: SimpleListCell<XStatement>) {
		cell.setOnDragOver { event ->
			if (event.gestureSource != cell) {
				if (event.dragboard.hasStatement()) {
					if (parents().none {
								it.hasClass("dragged")
							}) {
						event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
						for (p in parents()) {
							p.removeClass(Styles.dragover, Styles.dragoverFromBottom, Styles.dragoverFromTop)
						}
					}
				}
			}
			event.consume()
		}
		cell.setOnDragEntered { event ->
			if (event.gestureSource != cell && event.dragboard.hasContent(DATAFORMAT_XSTATEMENT)) {
				cell.addClass(Styles.dragover)
				if (event.y < cell.height / 2) {
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
			cell.removeClass(Styles.dragover, Styles.dragoverFromTop, Styles.dragoverFromBottom)
			event.consume()
		}
		cell.setOnDragDropped { event ->
			val rawStmt = event.dragboard.getContent(DATAFORMAT_XSTATEMENT) as? ByteArray
			if (rawStmt != null) {
				val xobj = XmllikeObject.fromBytes(rawStmt)
				val content = XStatementFromXmlObject(xobj)
				//					println("dropped $content generated from $xobj onto $stmt (from top = $wasDragFromTop)")
				val tgti = if (wasDragFromTop) cell.index else (cell.index + 1)
				cell.list.items.add(tgti, content)
				event.isDropCompleted = true
			} else {
				event.isDropCompleted = false
			}
			event.consume()
		}
	}
	
	class StatementCell(list: StatementListView, stmt: XStatement) :
			SimpleListCell<XStatement>(list, stmt),
			ContextMenuContainer {
		override val menus by lazy {
			listOf(Menu("Statement").apply {
				item("Delete", "Shortcut+Delete") {
					action {
						list.deleteStmt(stmt)
					}
				}
				menu("Insert _After").apply {
					for (e in StatementMetadata.entries) {
						if (e == null) separator()
						else item(e.name) {
							if (e.hotkey != null) accelerator = KeyCombination.valueOf("Shortcut+" + e.hotkey)
							action {
								list.insertAfter(stmt, e.factory(), true)
							}
						}
					}
				}
				menu("Insert _Before").apply {
					for (e in StatementMetadata.entries) {
						if (e == null) separator()
						else item(e.name) {
							if (e.hotkey != null) accelerator = KeyCombination.valueOf("Shortcut+Shift+" + e.hotkey)
							action {
								list.insertBefore(stmt, e.factory(), true)
							}
						}
					}
				}
			})
		}
	}
}