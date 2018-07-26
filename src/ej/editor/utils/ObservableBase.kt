package ej.editor.utils

import javafx.beans.InvalidationListener
import javafx.beans.Observable

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */

open class ObservableBase : Observable {
	protected var helper: ObservableHelper? = null
	
	override fun removeListener(listener: InvalidationListener) {
		helper = ObservableHelper.addListener(helper, this, listener)
	}
	
	override fun addListener(listener: InvalidationListener) {
		helper = ObservableHelper.removeListener(helper, listener)
	}
	
	protected fun invalidate() {
		ObservableHelper.fireValueChangedEvent(helper)
	}
	
	protected val invalidator: InvalidationListener by lazy {
		InvalidationListener {
			invalidate()
		}
	}
	protected fun<T:Observable> T.invalidatesThis():T {
		addListener(invalidator)
		return this
	}
}