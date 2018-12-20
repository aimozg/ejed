package ej.editor.utils

import ej.editor.expr.EnumChooser
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.WritableValue
import javafx.geometry.Pos
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import tornadofx.*
import kotlin.reflect.KProperty1

abstract class ValueChooser<T : Any> {
	abstract fun pickValue(title: String, initial: T? = null): T?
	fun pickValueFor(title: String, prop: WritableValue<T?>): T? {
		val v = pickValue(title, prop.value)
		if (v != null) prop.value = v
		return v
	}
}

abstract class AbstractListValueChooser<T : Any>(val items: List<T>) : ValueChooser<T>() {
	open fun formatter(item: T?): String = item?.toString() ?: "<???>"
	override fun pickValue(title: String, initial: T?): T? {
		return find<ListChooserDialog<T>>().showModal(title, initial, items) {
			text = formatter(item)
		}
	}
}

open class ListValueChooser<T : Any>(items: List<T>,
                                     val formatterFn: (T?) -> String) : AbstractListValueChooser<T>(items) {
	override fun formatter(item: T?): String = formatterFn(item)
	
}

inline fun <reified E : Enum<E>> EnumChooser(nameProperty: KProperty1<E, String>) =
		EnumChooser(enumValues(), nameProperty)

abstract class ChooserDialog<T : Any> : Fragment() {
	val resultProperty = SimpleObjectProperty<T>()
	var result: T? by resultProperty
	var ok: Boolean = false
	val okbtn = HBox()
	protected fun defaultRoot(init: VBox.() -> Unit) = vbox(5.0) {
		paddingAll = 10.0
		minWidth = 600.0
		minHeight = 250.0
		hgrow = Priority.ALWAYS
		init()
		this += okbtn.apply {
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
	
	protected fun showModal(title: String, initial: T?): T? {
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

open class ListChooserDialog<T : Any> : ChooserDialog<T>() {
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
		prefWidthProperty().bind(list.prefWidthProperty())
	}
	
	fun showModal(title: String,
	              initial: T?,
	              items: List<T?>,
	              formatter: ListCell<T>.(T) -> Unit): T? {
		this.items.setAll(items)
		list.cellFormat(formatter)
		list.selectionModel.select(initial)
		return showModal(title, initial ?: items.firstOrNull())
	}
}