package ej.editor.utils

import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.WeakChangeListener
import javafx.collections.ListChangeListener
import javafx.collections.ModifiableObservableListBase
import javafx.collections.ObservableList
import javafx.collections.WeakListChangeListener
import javafx.collections.transformation.FilteredList
import tornadofx.*
import java.util.*

/*
 * Created by aimozg on 05.07.2018.
 * Confidential until published on GitHub
 */

fun <T> ObservableValue<T>.onChangeAndNowWeak(op: (T?) -> Unit): ChangeListener<T> {
	op(value)
	val listener = ChangeListener<T> { _, _, newValue -> op(newValue) }
	addListener(WeakChangeListener(listener))
	return listener
}
fun <T> ObservableValue<T>.onChangeAndNow(op: (T?) -> Unit) {
	op(value)
	addListener{ _, _, newValue -> op(newValue) }
}
fun <T> ObservableValue<T>.onChangeWeak(op: (T?) -> Unit): ChangeListener<T> {
	val listener = ChangeListener<T>{ _, _, newValue -> op(newValue) }
	addListener(WeakChangeListener(listener))
	return listener
}
fun <T> ObservableList<T>.onChangeWeak(op: (ListChangeListener.Change<out T>) -> Unit): ListChangeListener<T> {
	val listener = ListChangeListener<T> { op(it) }
	addListener(WeakListChangeListener(listener))
	return listener
}

fun <I,O> ObservableList<I>.transformed(transform:(I)->O): ObservableList<O> {
	val source = this
	return ArrayList<O>().observable().apply {
		bind(source,transform)
	}
}

@Suppress("UNCHECKED_CAST")
inline fun <reified B> ObservableList<*>.filteredIsInstance(): FilteredList<B> {
	return filtered { it is B } as FilteredList<B>
}

fun<E> ObservableList<E>.filteredMutable(test:(E)->Boolean) = FilteredMutableList(this,test)
@Suppress("UNCHECKED_CAST")
inline fun<reified B> ObservableList<*>.filteredIsInstanceMutable() =
		filteredMutable { it is B } as ObservableList<B>

// TODO I fear it does not proxy change events correctly (indices are mangled)
class FilteredMutableList<E> private constructor(
		val source:ObservableList<E>,
		private val backingList:FilteredList<E>) :
		ModifiableObservableListBase<E>() {
	override fun get(index: Int): E = backingList[index]
	
	override val size: Int get() = backingList.size
	
	constructor(source: ObservableList<E>,test:(E)->Boolean): this(source,source.filtered(test))
	
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
