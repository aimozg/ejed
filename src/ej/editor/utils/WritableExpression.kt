package ej.editor.utils

import com.sun.javafx.binding.ExpressionHelper
import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.property.ObjectProperty
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import java.lang.ref.WeakReference

/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */
abstract class WritableExpression<T>
@JvmOverloads constructor(
		private val _bean: Any? = null,
		private val _name: String? = null
) : ObjectProperty<T>() {
	override fun getName() = _name
	override fun getBean() = _bean
	
	private var _value: T? = null
	private var valid = false
	private var observable: ObservableValue<out T>? = null
	private val listener: InvalidationListener by lazy {
		object : InvalidationListener {
			val wref = WeakReference(this@WritableExpression)
			override fun invalidated(observable: Observable) {
				val ref = wref.get()
				if (ref == null) observable.removeListener(this)
				else ref.markInvalid()
			}
		}
	}
	private var helper: ExpressionHelper<T>? = null
	
	protected abstract fun doGet(): T
	protected abstract fun doSet(value: T)
	
	final override fun addListener(listener: ChangeListener<in T>) {
		helper = ExpressionHelper.addListener(helper, this, listener)
	}
	
	final override fun addListener(listener: InvalidationListener) {
		helper = ExpressionHelper.addListener(helper, this, listener)
	}
	
	final override fun set(value: T) {
		if (isBound) {
			throw java.lang.RuntimeException((if (bean != null && name != null)
				bean?.javaClass?.simpleName + "." + name + " : "
			else
				"") + "A bound value cannot be set.")
		}
		if (_value != value) {
			_value = value
			doSet(value)
			markInvalid()
		}
	}
	
	override fun unbind() {
		observable?.let {
			value = it.value
			it.removeListener(listener)
			observable = null
		}
	}
	
	override fun removeListener(listener: ChangeListener<in T>?) {
		helper = ExpressionHelper.removeListener(helper, listener)
	}
	
	override fun removeListener(listener: InvalidationListener?) {
		helper = ExpressionHelper.removeListener(helper, listener)
	}
	
	override fun bind(observable: ObservableValue<out T>) {
		if (observable != this.observable) {
			unbind()
			this.observable = observable
			observable.addListener(listener)
			markInvalid()
		}
	}
	
	final override fun isBound() = observable != null
	
	final override fun get(): T {
		if (valid) {
			@Suppress("UNCHECKED_CAST")
			return _value as T
		}
		valid = true
		val x = doGet()
		_value = x
		return x
	}
	
	protected open fun fireValueChangedEvent() {
		ExpressionHelper.fireValueChangedEvent(helper)
	}
	
	fun markInvalid() {
		if (valid) {
			valid = false
			invalidated()
			fireValueChangedEvent()
		}
	}
	
	protected open fun invalidated() {
	}
}