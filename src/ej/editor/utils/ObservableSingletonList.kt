package ej.editor.utils

import javafx.beans.value.ObservableValue
import javafx.collections.ObservableListBase

/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

class ObservableSingletonList<T:Any>(val property:ObservableValue<out T?>): ObservableListBase<T>() {
	@Suppress("unused")
	private val listener = property.addWeakListener { _, oldValue, newValue ->
		if (oldValue == null && newValue != null) {
			beginChange()
			nextAdd(0, 1)
			endChange()
		} else if (oldValue != null && newValue == null) {
			beginChange()
			nextRemove(0, oldValue)
			endChange()
		} else if (oldValue != newValue) {
			beginChange()
			nextSet(0, oldValue)
			endChange()
		}
	}
	
	override fun get(index: Int): T {
		if (index != 0) throw IndexOutOfBoundsException("Index: $index")
		return property.value ?: throw IndexOutOfBoundsException("Empty")
	}
	
	override val size: Int
		get() = if (property.value == null) 0 else 1
	
}