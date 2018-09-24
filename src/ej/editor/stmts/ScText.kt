package ej.editor.stmts

import ej.editor.Styles
import ej.editor.utils.autoStretch
import ej.mod.XcText
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*

/*
 * Created by aimozg on 22.09.2018.
 * Confidential until published on GitHub
 */
class ScText(stmt: XcText) : StatementControl<XcText>(stmt) {
	override fun createDefaultSkin() = TextSkin()
	
	inner class TextSkin : ScSkin<XcText, ScText>(this) {
		override fun VBox.body() {
			addClass(Styles.xtext)
			textarea(stmt.textProperty()) {
				isWrapText = true
				hgrow = Priority.ALWAYS
				autoStretch()
			}
		}
	}
}

