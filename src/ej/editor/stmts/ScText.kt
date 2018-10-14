package ej.editor.stmts

import ej.editor.Styles
import ej.editor.views.FlashTextEditor
import ej.mod.XcText
import javafx.scene.layout.Priority
import tornadofx.*

/*
 * Created by aimozg on 22.09.2018.
 * Confidential until published on GitHub
 */
class ScText(stmt: XcText) : StatementControl<XcText>(stmt) {
	override fun createDefaultSkin() = TextSkin()
	
	inner class TextSkin : ScSkin<XcText, ScText>(this, {
		addClass(Styles.xtext)
		val editor = FlashTextEditor(stmt.textProperty()).apply {
			isWrapText = true
			hgrow = Priority.ALWAYS
			isAutoStretch = true
			disableScrollEvents()
		}
		children += editor
	})
}

