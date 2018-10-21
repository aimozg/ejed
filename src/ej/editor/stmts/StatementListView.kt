package ej.editor.stmts

import ej.editor.Styles
import ej.editor.stmts.old.DATAFORMAT_XSTATEMENT
import ej.editor.stmts.old.hasStatement
import ej.editor.utils.boundFaGlyph
import ej.editor.utils.fontAwesome
import ej.editor.utils.presentWhen
import ej.editor.views.DecoratedSimpleListView
import ej.mod.XStatement
import ej.mod.XStatementFromXmlObject
import ej.mod.toXmlObject
import ej.utils.addAfter
import ej.utils.addBefore
import ej.xml.XmllikeObject
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.geometry.Side
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.Label
import javafx.scene.control.Menu
import javafx.scene.input.TransferMode
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.StackPane
import javafx.scene.text.TextAlignment
import org.controlsfx.glyphfont.FontAwesome
import tornadofx.*

class StatementListView : DecoratedSimpleListView<XStatement>() {
	
	val expandedProperty = SimpleBooleanProperty(true)
	var expanded by expandedProperty
	
	lateinit var expandButton: Button; private set
	
	fun detachListMenu(): Node {
		beforeList = null
		return listTopMenu
	}
	
	fun attachListMenu() {
		listTopMenu.removeFromParent()
		beforeList = listTopMenu
	}
	
	private val listTopMenu = HBox().apply {
		addClass("stmt-ctrl-listmenu")
		expandButton = button {
			addClass("small-button")
			graphic = boundFaGlyph(expandedProperty.stringBinding { expanded ->
				if (expanded == true) FontAwesome.Glyph.CARET_DOWN.char.toString()
				else FontAwesome.Glyph.CARET_RIGHT.char.toString()
			})
			action {
				expanded = !expanded
			}
		}
		button {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.PLUS)
			contextmenu {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertAfter(null, e.factory())
						}
					}
				}
			}
			action {
				contextMenu.show(this, Side.BOTTOM, 0.0, 0.0)
			}
		}
	}
	val listBottomMenu = HBox().apply {
		addClass("stmt-ctrl-listmenu")
		button {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.PLUS)
			contextmenu {
				for (e in StatementMetadata.entries) {
					if (e == null) separator()
					else item(e.name/*, KeyCombination.valueOf(e.hotkey)*/) {
						action {
							insertBefore(null, e.factory())
						}
					}
				}
			}
			action {
				contextMenu.show(this, Side.BOTTOM, 0.0, 0.0)
			}
		}
	}
	
	fun deleteStmt(stmt: XStatement) {
		println("Deleting $stmt")
		items.remove(stmt)
	}
	
	fun insertAfter(ref: XStatement?, stmt: XStatement) {
		println("Inserting $stmt after $ref")
		items.addAfter(ref, stmt)
	}
	
	fun insertBefore(ref: XStatement?, stmt: XStatement) {
		println("Inserting $stmt before $ref")
		items.addBefore(ref, stmt)
	}
	
	private var wasDragFromTop = false
	init {
		graphicFactory { cell, stmt ->
			cell.setOnDragOver { event ->
				if (event.gestureSource != cell) {
					if (event.dragboard.hasStatement()) {
						event.acceptTransferModes(*TransferMode.COPY_OR_MOVE)
						for (p in generateSequence(parent) { it.parent }) {
							p.removeClass(Styles.dragover, Styles.dragoverFromBottom, Styles.dragoverFromTop)
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
					val content = XStatementFromXmlObject(XmllikeObject.fromBytes(rawStmt))
					println("dropped $content onto $stmt (from top = $wasDragFromTop)")
					val tgti = if (wasDragFromTop) cell.index else (cell.index + 1)
					cell.list.items.add(tgti, content)
					event.isDropCompleted = true
				} else {
					event.isDropCompleted = false
				}
				event.consume()
			}
			val cellMenu = cell.contextmenu {
				item("_Delete") {
					action {
						deleteStmt(stmt)
					}
				}
				menu("Insert _Before") {
					for (e in StatementMetadata.entries) {
						if (e == null) separator()
						else item(e.name) {
							action {
								insertBefore(stmt, e.factory())
							}
						}
					}
				}
				this += Menu("Insert _After").apply {
					for (e in StatementMetadata.entries) {
						if (e == null) separator()
						else item(e.name) {
							action {
								insertAfter(stmt, e.factory())
							}
						}
					}
				}
			}
			HBox().apply {
				alignment = Pos.TOP_LEFT
				children += StackPane().apply {
					addClass("stmt-ctrl-itemmenu")
					minWidth = Region.USE_PREF_SIZE
					minHeight = Region.USE_PREF_SIZE
					maxWidth = Region.USE_PREF_SIZE
					maxHeight = Region.USE_PREF_SIZE
					button().apply {
						addClass("small-button")
						graphic = fontAwesome.create(FontAwesome.Glyph.ELLIPSIS_H)
						action {
							cellMenu.show(this, Side.BOTTOM, 0.0, 0.0)
						}
						setOnDragDetected { event ->
							val db = cell.startDragAndDrop(TransferMode.MOVE)
							val content = stmt.toXmlObject()
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
				}
				children += (stmt.createControl() ?: Label("not supported ${stmt.javaClass.simpleName}").apply {
					contextMenu = cellMenu
					textAlignment = TextAlignment.LEFT
				}).apply {
					hgrow = Priority.ALWAYS
				}
			}
		}
		
		cells.onChange {
			while (it.next()) {
				for (cell in it.addedSubList) cell.presentWhen(expandedProperty)
			}
		}
		beforeList = listTopMenu
	}
}