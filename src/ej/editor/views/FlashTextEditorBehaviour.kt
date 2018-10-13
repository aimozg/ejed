package ej.editor.views

import javafx.scene.input.KeyCode.*
import javafx.scene.input.KeyCombination.SHORTCUT_DOWN
import org.fxmisc.wellbehaved.event.EventPattern.keyPressed
import org.fxmisc.wellbehaved.event.template.InputMapTemplate
import org.fxmisc.wellbehaved.event.template.InputMapTemplate.consume
import org.fxmisc.wellbehaved.event.template.InputMapTemplate.sequence

/*
 * Created by aimozg on 13.10.2018.
 * Confidential until published on GitHub
 */
class FlashTextEditorBehaviour(val view: FlashTextEditor) {
	init {
		InputMapTemplate.installFallback(EVENT_TEMPLATE, this) { it.view }
	}
	
	companion object {
		val EVENT_TEMPLATE: InputMapTemplate<FlashTextEditorBehaviour, *> =
				sequence(
						consume(keyPressed(B, SHORTCUT_DOWN)) { b, _ -> b.view.boldSelection() },
						consume(keyPressed(I, SHORTCUT_DOWN)) { b, _ -> b.view.italizeSelection() },
						consume(keyPressed(U, SHORTCUT_DOWN)) { b, _ -> b.view.underlineSelection() }
				)
	}
}