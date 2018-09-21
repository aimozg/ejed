package ej.editor.views

import ej.editor.Styles
import ej.editor.stmts.StatementListView
import ej.editor.utils.bindingN
import ej.editor.utils.observableUnique
import ej.mod.ModData
import ej.mod.XComplexStatement
import ej.mod.XStatement
import ej.mod.XcScene
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 18.09.2018.
 * Confidential until published on GitHub
 */
class SceneEditor(val mod: ModData) : VBox() {
	val rootStatementProperty = SimpleObjectProperty<XComplexStatement>(XcScene())
	var rootStatement: XComplexStatement by rootStatementProperty
	
	private val toolBar = GridPane()
	private val stmtList = StatementListView(
			bindingN(rootStatementProperty) {
				it?.content ?: emptyList<XStatement>().observableUnique()
			}
	)

	init {
		spacing = 5.0
		
		toolBar.apply {
			hgap = 5.0
			vgap = 5.0
			addClass(Styles.toolbarGrid)
			// TODO trigger controls and scene names
		}.attachTo(this)

		stmtList.attachTo(this)
	}
}