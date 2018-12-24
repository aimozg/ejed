package ej.editor.views

import org.fxmisc.richtext.StyleClassedTextArea

/*
 * Created by aimozg on 24.12.2018.
 * Confidential until published on GitHub
 */
class SceneXmlEditor() :
		StyleClassedTextArea() {
	init {
		// don't apply preceding style to typed text
		useInitialStyleForInsertion = true
		isWrapText = true
	}
	
	constructor(text: String) : this() {
		appendText(text)
		undoManager.forgetHistory()
		undoManager.mark()
		selectRange(0, 0)
	}
}