package ej.editor.utils

import javafx.collections.ModifiableObservableListBase
import javafx.collections.ObservableList
import javafx.collections.transformation.FilteredList

// TODO I fear it does not proxy change events correctly (indices are mangled)
class FilteredMutableList<E> private constructor(
		val source: ObservableList<E>,
		private val backingList: FilteredList<E>) :
		ModifiableObservableListBase<E>() {
	override fun get(index: Int): E = backingList[index]
	
	override val size: Int get() = backingList.size
	
	constructor(source: ObservableList<E>, test:(E)->Boolean): this(source, source.filtered(test))
	
	override fun doRemove(index: Int): E {
		return source.removeAt(backingList.getSourceIndex(index))
	}
	
	override fun doSet(index: Int, element: E): E {
		return source.set(backingList.getSourceIndex(index), element)
	}
	
	override fun doAdd(index: Int, element: E) {
		if (index < size) {
			source.add(backingList.getSourceIndex(index), element)
		} else {
			source.add(element)
		}
	}
	
	override fun indexOf(element: E): Int {
		return backingList.indexOf(element)
	}
	
	override fun lastIndexOf(element: E): Int {
		return backingList.lastIndexOf(element)
	}
	
	override operator fun contains(element: E): Boolean {
		return backingList.contains(element)
	}
	
	override fun containsAll(elements: Collection<E>): Boolean {
		return backingList.containsAll(elements)
	}
	
	/*
	@Suppress("unused")
	private val listener:ListChangeListener<E> = backingList.onChangeWeak {
		fireChange(it)
	}
	*/
}