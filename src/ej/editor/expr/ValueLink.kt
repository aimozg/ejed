package ej.editor.expr

import ej.editor.Styles
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.scene.text.Text
import tornadofx.*

/*
 * Created by aimozg on 17.07.2018.
 * Confidential until published on GitHub
 */

open class ValueLink<T:Any> : Text() {
	val titleProperty = SimpleStringProperty("")
	var title:String by titleProperty
	
	val valueProperty = SimpleObjectProperty<T>()
	var value:T? by valueProperty
	
	var chooser:ValueChooser<T>? = null
	
	var onPick :((T?)->Unit)? = null
	val textMakerProperty = SimpleObjectProperty<(T?)->String>({ it.toString() })
	var textMaker: (T?) -> String by textMakerProperty
	
	init {
		textProperty().bind(valueProperty.stringBinding(textMakerProperty) {
			val s = textMaker(it)
			if (s.isEmpty()) "<$title>" else s
		})
		bindClass(
				nonNullObjectBinding(valueProperty) {
					if (value == null) ej.editor.Styles.xexprBadLink else null
				}
		)
		addClass(Styles.xexprLink)
		setOnMouseClicked {
			val v = chooser?.pickValueFor(title, valueProperty)
			if (v != null) onPick?.invoke(v)
		}
	}
}