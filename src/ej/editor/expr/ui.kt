package ej.editor.expr

import ej.editor.Styles
import ej.editor.expr.impl.RawExpressionBuilder
import ej.editor.utils.AbstractListValueChooser
import ej.editor.utils.ChooserDialog
import ej.editor.utils.ValueChooser
import ej.editor.utils.nodeBinding
import javafx.beans.property.Property
import javafx.beans.value.WritableValue
import javafx.scene.layout.Priority
import javafx.scene.text.TextFlow
import tornadofx.*
import kotlin.reflect.KMutableProperty0

/*
 * Created by aimozg on 15.07.2018.
 * Confidential until published on GitHub
 */


inline fun defaultEditorTextFlow(cssClass: CssRule? = Styles.xexpr, init: TextFlow.() -> Unit): TextFlow {
	return TextFlow().apply {
		hgrow = Priority.ALWAYS
		if (cssClass != null) addClass(cssClass)
		/*
		minWidthProperty().bind(parentProperty().select {
			if (it is Region) {
				it.widthProperty().minus(it.paddingHorizontalProperty)
			} else DoubleConstant.valueOf(-1.0)
		})
		*/
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

class ExpressionChooserDialog : ChooserDialog<ExpressionBuilder>() {
	val items = ArrayList<ExpressionBuilder>().observable()
	
	override val root = defaultRoot {
		val cb = combobox(resultProperty, items) {
			hgrow = Priority.ALWAYS
			promptText = "-- Pick an option --"
			cellFormat {
				text = item?.name()?:"-- Pick an option --"
			}
		}
		nodeBinding(cb.selectionModel.selectedItemProperty()) { it ->
			it?.editorBody()?.also {
				currentStage?.sizeToScene()
			}
		}.apply {
			vgrow = Priority.ALWAYS
			hgrow = Priority.ALWAYS
		}
	}
	
	fun showModal(title:String,
	              initial:ExpressionBuilder?,
	              items:List<ExpressionBuilder>):ExpressionBuilder? {
		if (initial == null) {
			this.items.setAll(items)
		} else {
			this.items.setAll(items.map {
				when {
					it.initializableBy(initial) -> initial
					it is RawExpressionBuilder -> RawExpressionBuilder(initial.build())
					else -> it
				}
			})
		}
		return showModal(title, initial ?: items.firstOrNull())
	}
}