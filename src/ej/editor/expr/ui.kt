package ej.editor.expr

import ej.editor.Styles
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import tornadofx.*

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */


inline fun defaultBuilderBody(init: TextFlow.()->Unit): TextFlow {
	return TextFlow().apply {
		addClass(Styles.xexpr)
		hgrow = Priority.ALWAYS
		init()
	}
}
fun<T:Any> TextFlow.valueLink(title:String,
                              property: Property<T?>,
                              chooser:ValueChooser<T>,
                              textMaker:(T?)->String) {
	text(property.stringBinding {
		val s = textMaker(it)
		if (s.isEmpty()) "<$title>" else s
	}) {
		bindClass(
				nonNullObjectBinding(property) {
					if (value == null) Styles.xexprBadLink else null
				}
		)
		addClass(Styles.xexprLink)
		setOnMouseClicked {
			chooser.pickValueFor(title, property)
		}
	}
}
fun<T:Any> TextFlow.valueLink(title:String,
                              property: Property<T?>,
                              chooser:AbstractListValueChooser<T>) {
	valueLink(title, property, chooser, chooser::formatter)
}
fun TextFlow.valueLink(title:String,
                       property: Property<ExpressionBuilder?>,
                       chooser:ExpressionChooser,
                       defaultText:String="<$title>") {
	valueLink(title, property,chooser) {
		it?.text()?:defaultText
	}
}
abstract class ChooserDialog<T:Any> : Fragment() {
	val resultProperty = SimpleObjectProperty<T>()
	var result:T? by resultProperty
	var ok:Boolean = false
	protected fun defaultRoot(init:VBox.()->Unit) = vbox(5.0) {
		paddingAll = 10.0
		minWidth = 300.0
		minHeight = 200.0
		init()
		hbox {
			alignment = Pos.BASELINE_RIGHT
			button("OK") {
				isDefaultButton = true
				action {
					ok = true
					close()
				}
			}
		}
	}
	protected fun showModal(title:String, initial:T?):T? {
		this.title = title
		this.result = initial
		this.ok = false
		openModal(block = true)
		return if (ok) result else null
	}
	
	override fun onDock() {
		currentStage?.apply {
			sizeToScene()
			minWidth = width
			minHeight = height
		}
	}
}
open class ListChooserDialog<T:Any> : ChooserDialog<T>() {
	val items = ArrayList<T>().observable()
	var list: ListView<T> by singleAssign()
	override val root = defaultRoot {
		listview(items) {
			list = this
			hgrow = Priority.ALWAYS
			selectionModel.selectedItemProperty().onChange {
				result = it
			}
		}
	}
	fun showModal(title:String,
	              initial: T?,
	              items: List<T?>,
	              formatter: ListCell<T>.(T) -> Unit):T? {
		this.items.setAll(items)
		list.cellFormat(formatter)
		list.selectionModel.select(initial)
		return showModal(title,initial)
	}
}
class ExpressionChooserDialog : ChooserDialog<ExpressionBuilder>() {
	val items = ArrayList<ExpressionBuilder>().observable()
	var editorContainer: Pane by singleAssign()
	var exprEditor: Pane = hbox()
	
	override val root = defaultRoot {
		combobox(resultProperty,items) {
			hgrow = Priority.ALWAYS
			promptText = "-- Pick an option --"
			cellFormat {
				text = item?.name()?:"-- Pick an option --"
			}
			selectionModel.selectedItemProperty().onChange { eb ->
				exprEditor.removeFromParent()
				exprEditor = eb?.editorBody()?:hbox()
				editorContainer += exprEditor
			}
		}
		pane {
			editorContainer = this
			minWidth = 300.0
			minHeight = 200.0
			this += exprEditor
		}
	}
	
	fun showModal(title:String,
	              initial:ExpressionBuilder?,
	              items:List<ExpressionBuilder>):ExpressionBuilder? {
		if (initial == null) {
			this.items.setAll(items)
		} else {
			this.items.setAll(items.map {
				if (it.javaClass == initial.javaClass) initial else it
			})
		}
		return showModal(title,initial)
	}
}