package ej.editor.utils

import javafx.beans.InvalidationListener
import javafx.beans.Observable

/*
 * Created by aimozg on 18.07.2018.
 * Confidential until published on GitHub
 */

abstract class ObservableHelper {
	protected abstract fun addListener(listener: InvalidationListener): ObservableHelper
	protected abstract fun removeListener(listener: InvalidationListener): ObservableHelper?
	protected abstract fun fireValueChangedEvent()
	
	companion object {
		fun addListener(helper: ObservableHelper?, observable:Observable, listener: InvalidationListener): ObservableHelper =
				helper?.addListener(listener)?:SingleInvalidation(observable, listener)
		fun removeListener(helper: ObservableHelper?, listener: InvalidationListener): ObservableHelper? =
				helper?.removeListener(listener)
		fun fireValueChangedEvent(helper: ObservableHelper?) {
			helper?.fireValueChangedEvent()
		}
	}
	private class SingleInvalidation(val observable:Observable, val listener:InvalidationListener) : ObservableHelper() {
		override fun addListener(listener: InvalidationListener): ObservableHelper =
				Generic(observable,this.listener,listener)
		
		override fun removeListener(listener: InvalidationListener): ObservableHelper? = null
		
		override fun fireValueChangedEvent() {
			listener.invalidated(observable)
		}
		
	}
	private class Generic(val observable:Observable, listener0:InvalidationListener, listener1:InvalidationListener) : ObservableHelper() {
		private var listeners = arrayListOf(listener0,listener1)
		private var locked = false
		private var newListeners: ArrayList<InvalidationListener>? = null
		
		override fun addListener(listener: InvalidationListener): ObservableHelper {
			if (locked && newListeners == null) newListeners = ArrayList(listeners)
			(newListeners?:listeners).add(listener)
			return this
		}
		
		override fun removeListener(listener: InvalidationListener): ObservableHelper {
			if (locked && newListeners == null) newListeners = ArrayList(listeners)
			val current = newListeners?:listeners
			current.remove(listener)
			if (current.size == 1) return SingleInvalidation(observable, current[0])
			return this
		}
		
		override fun fireValueChangedEvent() {
			locked = true
			for (i in ArrayList(listeners)) {
				i.invalidated(observable)
			}
			locked = false
			newListeners?.let { listeners = it }
		}
		
	}
}