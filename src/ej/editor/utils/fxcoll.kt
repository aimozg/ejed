package ej.editor.utils

import javafx.beans.binding.Bindings
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
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
import java.util.concurrent.Callable

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

fun <I : ObservableList<*>, O: Any> I.listBinding(calculator:(I)->O): Property<O> {
	val list = this
	return object:SimpleObjectProperty<O>(calculator(this)) {
		@Suppress("unused")
		val listener = list.onChangeWeak {
			value = calculator(list)
		}
	}
}

fun <I,O> ObservableList<I>.transformed(transform:(I)->O): ObservableList<O> {
	val source = this
	return ArrayList<O>().observable().apply {
		bind(source,transform)
	}
}

fun <T1,R> binding1(prop1:ObservableValue<T1>,
                    op:(T1?)->R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value)
		}, prop1)

fun <T1,T2,R> binding2(prop1:ObservableValue<T1>,
                       prop2:ObservableValue<T2>,
                       op:(T1?,T2?)->R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value,prop2.value)
		}, prop1, prop2)

fun <T1,T2,T3,R> binding3(prop1:ObservableValue<T1>,
                          prop2:ObservableValue<T2>,
                          prop3:ObservableValue<T3>,
                          op:(T1?,T2?,T3?)->R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value,prop2.value,prop3.value)
		}, prop1, prop2, prop3)

fun <T1,T2,T3,T4,R> binding4(prop1:ObservableValue<T1>,
                          prop2:ObservableValue<T2>,
                          prop3:ObservableValue<T3>,
                          prop4:ObservableValue<T4>,
                          op:(T1?,T2?,T3?,T4?)->R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value,prop2.value,prop3.value,prop4.value)
		}, prop1, prop2, prop3,prop4)

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
