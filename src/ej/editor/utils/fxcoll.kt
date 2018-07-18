package ej.editor.utils

import com.sun.javafx.collections.ObservableListWrapper
import javafx.beans.Observable
import javafx.beans.WeakInvalidationListener
import javafx.beans.binding.Bindings
import javafx.beans.binding.BooleanBinding
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.beans.value.WeakChangeListener
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import javafx.collections.WeakListChangeListener
import javafx.collections.transformation.FilteredList
import javafx.util.StringConverter
import tornadofx.*
import java.util.concurrent.Callable
import java.util.concurrent.atomic.AtomicInteger
import kotlin.reflect.KProperty1

/*
 * Created by aimozg on 05.07.2018.
 * Confidential until published on GitHub
 */

fun ObservableValue<out String?>.isNullOrEmpty(): BooleanBinding =
		booleanBinding { it.isNullOrEmpty() }

fun <T> ObservableValue<T>.addWeakListener(fn:(observale:ObservableValue<out T>, oldValue:T?, newValue:T?)->Unit): ChangeListener<T>{
	val listener = ChangeListener(fn)
	addListener(WeakChangeListener(listener))
	return listener
}
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

fun <I,O> ObservableList<I>.transformed(transform:(I)->O): ObservableList<O> = TransformingList(this,transform)
fun <T> List<T>.observableUnique():ObservableList<T> = object: ObservableListWrapper<T>(this.observable()) {
	override fun hashCode(): Int {
		return System.identityHashCode(this)
	}
	
	override fun equals(other: Any?): Boolean {
		return this === other
	}
}

open class ObservableMutableProperty<T:Observable?>(initialValue:T): SimpleObjectProperty<T>(initialValue) {
	private val listener = WeakInvalidationListener{
		this@ObservableMutableProperty.fireValueChangedEvent()
	}
	override fun set(v: T) {
		value?.removeListener(listener)
		super.set(v)
		v?.addListener(listener)
	}
}

fun <T1, R> bindingN(prop1: ObservableValue<T1>,
                     op: (T1?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value)
		}, prop1)

fun <T1, T2, R> bindingN(prop1: ObservableValue<T1>,
                         prop2: ObservableValue<T2>,
                         op: (T1?, T2?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value)
		}, prop1, prop2)

fun <T1, T2, T3, R> bindingN(prop1: ObservableValue<T1>,
                             prop2: ObservableValue<T2>,
                             prop3: ObservableValue<T3>,
                             op: (T1?, T2?, T3?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value, prop3.value)
		}, prop1, prop2, prop3)

fun <T1, T2, T3, T4, R> bindingN(prop1: ObservableValue<T1>,
                                 prop2: ObservableValue<T2>,
                                 prop3: ObservableValue<T3>,
                                 prop4: ObservableValue<T4>,
                                 op: (T1?, T2?, T3?, T4?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value, prop3.value, prop4.value)
		}, prop1, prop2, prop3, prop4)

fun <T1, T2, T3, T4, T5, R> bindingN(prop1: ObservableValue<T1>,
                                     prop2: ObservableValue<T2>,
                                     prop3: ObservableValue<T3>,
                                     prop4: ObservableValue<T4>,
                                     prop5: ObservableValue<T5>,
                                     op: (T1?, T2?, T3?, T4?, T5?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value, prop3.value, prop4.value, prop5.value)
		}, prop1, prop2, prop3, prop4, prop5)

fun <T1, T2, T3, T4, T5, T6, R> bindingN(prop1: ObservableValue<T1>,
                                         prop2: ObservableValue<T2>,
                                         prop3: ObservableValue<T3>,
                                         prop4: ObservableValue<T4>,
                                         prop5: ObservableValue<T5>,
                                         prop6: ObservableValue<T6>,
                                         op: (T1?, T2?, T3?, T4?, T5?, T6?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value, prop3.value, prop4.value, prop5.value, prop6.value)
		}, prop1, prop2, prop3, prop4, prop5, prop6)

fun <T1, T2, T3, T4, T5, T6, T7, R> bindingN(prop1: ObservableValue<T1>,
                                             prop2: ObservableValue<T2>,
                                             prop3: ObservableValue<T3>,
                                             prop4: ObservableValue<T4>,
                                             prop5: ObservableValue<T5>,
                                             prop6: ObservableValue<T6>,
                                             prop7: ObservableValue<T7>,
                                             op: (T1?, T2?, T3?, T4?, T5?, T6?, T7?) -> R): ObservableValue<R> =
		Bindings.createObjectBinding(Callable {
			op(prop1.value, prop2.value, prop3.value, prop4.value, prop5.value, prop6.value, prop7.value)
		}, prop1, prop2, prop3, prop4, prop5, prop6, prop7)

@Suppress("UNCHECKED_CAST")
inline fun <reified B> ObservableList<*>.filteredIsInstance(): FilteredList<B> {
	return filtered { it is B } as FilteredList<B>
}

fun<E> ObservableList<E>.filteredMutable(test:(E)->Boolean) = FilteredMutableList(this,test)
@Suppress("UNCHECKED_CAST")
inline fun<reified B> ObservableList<*>.filteredIsInstanceMutable() =
		filteredMutable { it is B } as ObservableList<B>

fun<E> observableConcatenation(vararg lists:ObservableList<out E>):ObservableList<E> =
		AggregatedObservableArrayList(*lists).aggregatedList

fun stringValueToggler(source: Property<out String?>,defaultTrueValue:String) = object:SimpleObjectProperty<Boolean>() {
	override fun set(v: Boolean?) {
		if (v == true) {
			val srcVal = source.value
			if (srcVal.isNullOrEmpty()) {
				source.value = defaultTrueValue
			}
		} else {
			source.value = ""
		}
	}
	init {
		bind(source.isNullOrEmpty().not())
	}
}
inline fun AtomicInteger.callDepthTracking(code:()->Unit):Int {
	incrementAndGet()
	try {
		code()
	} finally {
		return decrementAndGet()
	}
}
inline fun AtomicInteger.callAtZero(code:()->Unit):Boolean {
	if (compareAndSet(0, 1)) {
		try {
			code()
		} finally {
			decrementAndGet()
		}
		return true
	}
	return false
}
object NullableIntStringConverter : StringConverter<Int?>() {
	override fun toString(`object`: Int?): String {
		return `object`?.toString()?:""
	}
	
	override fun fromString(string: String?): Int? {
		return string?.toIntOrNull()
	}
	
}

fun <THIS : Any> THIS.withPropertiesFrom(from: THIS,
                                         vararg properties: KProperty1<THIS, Property<*>>): THIS {
	for (property in properties) {
		copyProperty(property, from)
	}
	return this
}

fun <THIS : Any> THIS.copyProperty(
		prop: KProperty1<THIS, Property<*>>,
		from: THIS) {
	prop.get(this).value = prop.get(from).value
}