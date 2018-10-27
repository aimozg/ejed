package ej.editor.views

import ej.editor.stmts.SceneTriggerEditor
import ej.editor.stmts.StatementListView
import ej.editor.utils.bindingN
import ej.editor.utils.nodeBinding
import ej.editor.utils.observableUnique
import ej.mod.ModData
import ej.mod.XComplexStatement
import ej.mod.XStatement
import ej.mod.XcScene
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Orientation
import javafx.scene.control.ScrollPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 18.09.2018.
 * Confidential until published on GitHub
 */
class SceneEditor(val mod: ModData) : VBox() {
	val rootStatementProperty = SimpleObjectProperty<XComplexStatement>(XcScene())
	var rootStatement: XComplexStatement by rootStatementProperty
	
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	private val stmtList: StatementListView

	init {
		nodeBinding(rootStatementProperty) { it ->
				(it as? XcScene)?.let {
					SceneTriggerEditor(it)
				}
			}
		stmtList = StatementListView().apply {
			vgrow = Priority.SOMETIMES
			expandButton.removeFromParent()
			itemsProperty.bind(bindingN(rootStatementProperty) {
				it?.content ?: emptyList<XStatement>().observableUnique()
			})
			beforeList = listTopMenu
			afterList = listBottomMenu
			paddingBottom = 80
		}
		scrollpane(true, false) {
			hbarPolicy = ScrollPane.ScrollBarPolicy.NEVER
			vgrow = Priority.ALWAYS
			stmtList.attachTo(this)
		}
	}
}