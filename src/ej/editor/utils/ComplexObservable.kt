package ej.editor.utils

import javafx.beans.InvalidationListener
import javafx.beans.Observable
import javafx.beans.property.Property
import tornadofx.*
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/*
 * Created by aimozg on 14.07.2018.
 * Confidential until published on GitHub
 */

abstract class ComplexObservable : Observable {
	private val listeners = Collections.synchronizedList(ArrayList<InvalidationListener>())
	private val registeredProperties = ArrayList<Property<*>>()
	private val nChanges = AtomicInteger(0)
	private val dirty = AtomicBoolean(false)
	protected fun<T: Property<*>> T.register():T {
		registeredProperties += this
		onChange {
			invalidate()
		}
		return this
	}
	protected fun invalidate() {
		val n = nChanges.callDepthTracking {
			dirty.set(true)
		}
		if (n == 0 && dirty.get()) {
			fireEvent()
		}
	}
	protected fun fireEvent() {
		if (dirty.compareAndSet(true,false)) {
			synchronized(listeners) {
				for (l in listeners) {
					l.invalidated(this)
				}
			}
		}
	}
	override fun removeListener(listener: InvalidationListener) {
		listeners -= listener
	}
	
	override fun addListener(listener: InvalidationListener) {
		listeners += listener
	}
}