package ej.editor.views

import ej.editor.Styles
import ej.mod.XcStyledText
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

class TextNodeLabel(tree:StatementTree, stmt: XcStyledText): VBox() {
	init {
		val g = this
		val fnExpanded = tree.expandedNodesProperty.toBinding()
		val fnCollapsed = fnExpanded.not()
		textflow {
			prefWidthProperty().bind(g.widthProperty())
			maxWidthProperty().bind(g.widthProperty())
			for (run in stmt.runs) {
				text(run.content) {
					style = run.style.toCss()
					addClass(Styles.xtext)
				}
			}
			hiddenWhen(fnCollapsed)
			managedWhen(fnExpanded)
		}
		label(stmt.textContent.replace("\n"," ")) {
			addClass(Styles.xtext)
			hiddenWhen(fnExpanded)
			managedWhen(fnCollapsed)
		}
	}
}