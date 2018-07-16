package ej.editor.utils

import javafx.collections.ObservableList
import tornadofx.*

/*
 * Created by aimozg on 16.07.2018.
 * Confidential until published on GitHub
 */
class TransformingList<I,O>(
		source: ObservableList<I>,
		transform:(I)->O): ObservableList<O> by ArrayList<O>().observable(){
	@Suppress("unused")
	private val listener = bind(source, transform)
	
	override fun equals(other: Any?): Boolean {
		return this === other
	}
	override fun hashCode(): Int {
		return System.identityHashCode(this)
	}
}

