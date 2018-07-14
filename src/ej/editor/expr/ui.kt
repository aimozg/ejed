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
fun<T:Any> TextFlow.valueLink(property: Property<T>,
                              chooser:ValueChooser<T>,
                              textMaker:(T?)->String) {
	text(property.stringBinding {
		textMaker(it)
	}) {
		bindClass(
				nonNullObjectBinding(property) {
					if (value == null) Styles.xexprBadLink else null
				}
		)
		addClass(Styles.xexprLink)
		setOnMouseClicked {
			chooser.pickValueFor(property)
		}
	}
}
fun<T:Any> TextFlow.valueLink(property: Property<T>,
                              chooser:ListValueChooser<T>) {
	valueLink(property, chooser, chooser.formatter)
}
fun TextFlow.valueLink(property: Property<ExpressionBuilder>,
                       chooser:ExpressionChooser,
                       defaultText:String) {
	valueLink(property,chooser) {
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
	protected fun showModal(initial:T?):T? {
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
class ListChooserDialog<T:Any> : ChooserDialog<T>() {
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
	fun showModal(initial: T?,
	              items: List<T?>,
	              formatter: ListCell<T>.(T) -> Unit):T? {
		this.items.setAll(items)
		list.cellFormat(formatter)
		list.selectionModel.select(initial)
		return showModal(initial)
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
	
	fun showModal(initial:ExpressionBuilder?, items:List<ExpressionBuilder>):ExpressionBuilder? {
		if (initial == null) {
			this.items.setAll(items)
		} else {
			this.items.setAll(items.map {
				if (it.javaClass == initial.javaClass) initial else it
			})
		}
		return showModal(initial)
	}
}
/*
class TextInputDialog : View() {
	var result:String? = null
	
	val labelProperty = SimpleObjectProperty("")
	var label by labelProperty
	
	val initialValueProperty = SimpleObjectProperty("")
	var initialValue by initialValueProperty
	
	val cancelableProperty = SimpleObjectProperty(false)
	var cancelable by cancelableProperty
	
	fun showModal(title:String,label:String,initialValue:String="",cancelable:Boolean=true):String? {
		this.title = title
		this.label = label
		this.initialValue = initialValue
		this.cancelable = cancelable
		this.result = null
		openModal(block = true)
		return result
	}
	override val root = form {
		fieldset {
			lateinit var input: TextField
			field("Label") {
				label.textProperty().bind(labelProperty)
				input = textfield(initialValueProperty)
			}
			hbox(20) {
				alignment = Pos.BASELINE_CENTER
				button("Ok") {
					shortcut("Return")
					action {
						result = input.text
						close()
					}
				}
				button("Cancel") {
					shortcut("Escape")
					visibleWhen(cancelableProperty)
					action {
						result = null
						close()
					}
				}
			}
		}
	}
}
inline fun<T> UIComponent.textInputDialog(title:String,
                                          label:String,
                                          initialValue:String="",
                                          cancelable:Boolean=true,
                                          handler:(String)->T):T? {
	return textInputDialog(title, label, initialValue, cancelable)?.let(handler)
}
fun textInputDialog(title:String,label:String,initialValue:String="",cancelable:Boolean=true):String? {
	return find<TextInputDialog>().showModal(title, label, initialValue, cancelable)
}
 */