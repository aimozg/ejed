package ej.editor.stmts

import ej.editor.utils.fontAwesome
import ej.editor.utils.presentWhen
import ej.editor.views.DecoratedSimpleListView
import ej.mod.XStatement
import javafx.beans.property.SimpleBooleanProperty
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Label
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
	
	fun detachListMenu(): Node {
		beforeList = null
		return listMenu
	}
	
	fun attachListMenu() {
		listMenu.removeFromParent()
		beforeList = listMenu
	}
	
	private val listMenu = HBox().apply {
		addClass("stmt-ctrl-listmenu")
		button {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.CARET_DOWN).apply {
				textProperty().bind(expandedProperty.stringBinding { expanded ->
					if (expanded == true) FontAwesome.Glyph.CARET_DOWN.char.toString()
					else FontAwesome.Glyph.CARET_RIGHT.char.toString()
				})
			}
			action {
				expanded = !expanded
			}
		}
		button {
			addClass("small-button")
			graphic = fontAwesome.create(FontAwesome.Glyph.PLUS)
		}
	}
	
	init {
		graphicFactory {
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
					}
				}
				children += (it.createControl() ?: Label("not supported ${it.javaClass.simpleName}").apply {
					textAlignment = TextAlignment.LEFT
				}).apply {
					hgrow = Priority.ALWAYS
				}
			}
		}
		beforeList = listMenu
		cellContainer.presentWhen(expandedProperty)
	}
}