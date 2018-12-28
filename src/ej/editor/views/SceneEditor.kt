package ej.editor.views

import com.sun.javafx.scene.control.skin.ScrollBarSkin
import ej.editor.stmts.SceneTriggerEditor
import ej.editor.stmts.StatementListView
import ej.editor.utils.ContextMenuContainer
import ej.editor.utils.bindingN
import ej.editor.utils.nodeBinding
import ej.editor.utils.observableUnique
import ej.mod.*
import ej.xml.XmlExplorer
import ej.xml.deserializeDocument
import ej.xml.getSerializationInfo
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.control.Alert
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 18.09.2018.
 * Confidential until published on GitHub
 */
class SceneEditor(val mod: ModData) : VBox(), ContextMenuContainer {
	val rootStatementProperty = SimpleObjectProperty<XComplexStatement>(XcScene())
	var rootStatement: XComplexStatement by rootStatementProperty
	
	var xmlModeProperty = object : SimpleBooleanProperty(false) {
		override fun set(newValue: Boolean) {
			val oldValue = value
			if (trySetXmlMode(newValue)) {
				super.set(newValue)
			} else {
				super.set(newValue)
				runLater {
					super.set(oldValue)
				}
			}
		}
	}
	
	private fun trySetXmlMode(xm: Boolean): Boolean {
		val stmt = rootStatement
		when (xm) {
			true -> {
				xmlEditor.replaceText("")
				xmlEditor.replaceText(
						stmt.content.joinToString(separator = "\n") {
							val e = ((it as?XlIf)?.ungrouped() ?: it)
							e.toPrettyPrintedXml(false)
						})
			}
			false -> try {
				val tmpScene = getSerializationInfo<XcNamedText>().deserializeDocument(
						XmlExplorer("<?xml version=\"1.0\"?><text>\n${xmlEditor.text}\n</text>")
				)
				stmt.content.setAll(tmpScene.content)
			} catch (e: Throwable) {
				alert(Alert.AlertType.ERROR, "Error", e.message)
				if (e is XmlExplorer.XmlExplorerException && (e.lineNumber - 2) in xmlEditor.paragraphs.indices) {
					xmlEditor.selectRange(e.lineNumber - 2, e.columnNumber - 1, e.lineNumber - 2, e.columnNumber - 1)
				}
				return false
			}
		}
		return true
	}
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	private val stmtList: StatementListView
	private val xmlEditor: SceneXmlEditor
	override val menus get() = stmtList.menus
	
	init {
		isFillWidth = true
		stmtList = StatementListView().apply {
			vgrow = Priority.SOMETIMES
			expandButton.removeFromParent()
			itemsProperty.bind(bindingN(rootStatementProperty) {
				it?.content ?: emptyList<XStatement>().observableUnique()
			})
			paddingBottom = 80
		}
		xmlEditor = SceneXmlEditor().apply {
			prefHeightProperty().bind(this@SceneEditor.heightProperty())
		}
		
		toolbar {
			togglegroup {
				togglebutton("XML Mode", value = true) {
					addClass("nogap")
				}
				togglebutton("Visual Mode", value = false) {
					addClass("nogap")
				}
				bind(xmlModeProperty)
			}
		}
		nodeBinding(xmlModeProperty) { xmlMode ->
			if (xmlMode == true) {
				VBox().apply {
					spacing = 5.0
					isFillWidth = true
					xmlEditor.attachTo(this)
				}
			} else {
				VBox().apply {
					nodeBinding(rootStatementProperty) { it ->
						(it as? XcScene)?.let {
							SceneTriggerEditor(it)
						}
					}
					scrollpane(true, false) {
						hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
						vgrow = Priority.ALWAYS
						prefViewportWidthProperty().bind(widthProperty() - ScrollBarSkin.DEFAULT_WIDTH)
						stmtList.attachTo(this)
					}
				}
			}
		}
	}
}