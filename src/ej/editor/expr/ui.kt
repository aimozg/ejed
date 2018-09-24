package ej.editor.expr

import com.sun.javafx.binding.DoubleConstant
import ej.editor.Styles
import ej.editor.expr.impl.RawExpressionBuilder
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.WritableValue
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.text.TextFlow
import tornadofx.*
import kotlin.reflect.KMutableProperty0

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */


inline fun defaultEditorTextFlow(cssClass: CssRule? = Styles.xexpr, init: TextFlow.() -> Unit): TextFlow {
	return TextFlow().apply {
		if (cssClass != null) addClass(cssClass)
		prefWidthProperty().bind(parentProperty().select {
			if (it is Region) {
				it.widthProperty().minus(it.paddingHorizontalProperty)
			} else DoubleConstant.valueOf(-1.0)
		})
		init()
	}
}

fun <T : Any> TextFlow.valueLink(property: WritableValue<T?>,
                                 title: String,
                                 chooser: ValueChooser<T>,
                                 textMaker: (T?) -> String) =
		ValueLink<T>().attachTo(this) {
			this.title = title
			if (property is Property<T?>) {
				this.valueProperty.bindBidirectional(property)
			} else {
				this.value = property.value
				this.valueProperty.onChange { property.value = it }
			}
			this.chooser = chooser
			this.textMaker = textMaker
		}

fun <T : Any> TextFlow.valueLink(title: String,
                                 initialValue: T?,
                                 chooser: ValueChooser<T>,
                                 setter: (T?) -> Unit,
                                 textMaker: (T?) -> String): ValueLink<T> =
		ValueLink<T>().attachTo(this) {
			this.title = title
			this.value = initialValue
			this.onPick = setter
			this.chooser = chooser
			this.textMaker = textMaker
		}

fun <T : Any> TextFlow.valueLink(property: KMutableProperty0<T>,
                                 title: String,
                                 chooser: ValueChooser<T>,
                                 textMaker: (T?) -> String) =
		valueLink(title,
		          property.get(),
		          chooser,
		          { if (it != null) property.set(it) },
		          textMaker)

fun TextFlow.valueLink(property: KMutableProperty0<ExpressionBuilder?>,
                       title: String,
                       chooser: ExpressionChooser,
                       defaultText: String = "<$title>") =
		valueLink(title,
		          property.get(),
		          chooser,
		          setter = { property.set(it) },
		          textMaker = { it?.text() ?: defaultText })

fun <T : Any> TextFlow.valueLink(property: Property<T?>,
                                 title: String,
                                 chooser: AbstractListValueChooser<T>) =
		valueLink(property, title, chooser, chooser::formatter)

fun TextFlow.valueLink(property: Property<ExpressionBuilder?>,
                       title: String,
                       chooser: ExpressionChooser,
                       defaultText: String = "<$title>") =
		valueLink(property, title, chooser) {
			it?.text() ?: defaultText
		}

fun TextFlow.valueLink(title: String,
                       initialValue: ExpressionBuilder?,
                       chooser: ExpressionChooser,
                       setter: (ExpressionBuilder?) -> Unit,
                       defaultText: String = "<$title>") =
		valueLink(title, initialValue, chooser, setter) {
			it?.text() ?: defaultText
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
		return showModal(title,initial ?: items.firstOrNull())
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
		hbox {
			editorContainer = this
			vgrow = Priority.ALWAYS
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
				if (it.javaClass == initial.javaClass) initial
				else if (it is RawExpressionBuilder) RawExpressionBuilder(initial.build())
				else it
			})
		}
		return showModal(title, initial ?: items.firstOrNull())
	}
}