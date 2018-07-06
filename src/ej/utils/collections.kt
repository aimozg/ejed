package ej.utils

import javafx.collections.ObservableList
import tornadofx.*

/*
 * Created by aimozg on 03.07.2018.
 * Confidential until published on GitHub
 */

fun<T:Any?> T.addToList(list:MutableList<T>):T {
	list.add(this)
	return this
}

fun<T> MutableList<T>.pop():T {
	return removeAt(lastIndex)
}
fun<T> MutableList<T>.peek():T {
	return get(lastIndex)
}
fun<T> MutableList<T>.push(e:T) {
	add(e)
}
fun<T> MutableList<T>.longSwap(i:Int, j:Int):Boolean {
	if (i == j || i !in indices || j !in indices) return false
	if (this is ObservableList<T>) {
		// We cannot use swap()
		// because it is 2 set()s
		// and after 1st set() event is fired while there are duplicates in the list
		if (i > j) return longSwap(j, i)
		val b = removeAt(j)
		val a = removeAt(i)
		add(i, b)
		add(j, a)
	} else {
		swap(i,j)
	}
	return true
}
