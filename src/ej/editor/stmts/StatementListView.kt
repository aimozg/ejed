package ej.editor.stmts

import ej.editor.views.DecoratedSimpleListView
import ej.mod.XStatement
import javafx.scene.control.Label
import javafx.scene.text.Text
import javafx.scene.text.TextAlignment

class StatementListView : DecoratedSimpleListView<XStatement>() {
	
	init {
		graphicFactory {
			it.createControl() ?: Label("not supported ${it.javaClass.simpleName}").apply {
				textAlignment = TextAlignment.LEFT
			}
		}
		
		beforeList = Text("Before List")
		beforeCell {
			Text("Before ${it.javaClass.simpleName}")
		}
		afterCell {
			Text("After ${it.javaClass.simpleName}")
		}
		afterList = Text("After List")
	}
}