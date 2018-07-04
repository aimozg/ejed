package ej.editor.views

import ej.editor.Styles
import ej.mod.XcText
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

class TextNodeLabel(tree:StatementTree, stmt: XcText): VBox() {
	init {
		val g = this
		val fnExpanded = tree.expandedNodesProperty.toBinding()
		val fnCollapsed = fnExpanded.not()
		textflow {
			prefWidthProperty().bind(g.widthProperty())
			maxWidthProperty().bind(g.widthProperty())
			text(stmt.textProperty())
			hiddenWhen(fnCollapsed)
			managedWhen(fnExpanded)
		}
		label(stmt.textProperty().stringBinding {it?.replace("\n"," ")}) {
			addClass(Styles.xtext)
			hiddenWhen(fnExpanded)
			managedWhen(fnCollapsed)
		}
	}
}
