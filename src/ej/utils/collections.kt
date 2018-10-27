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
fun<T> MutableList<T>.removeLast():T {
	return removeAt(lastIndex)
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

fun <T> MutableList<T>.remove(fromIndex: Int, toIndex: Int) {
	subList(fromIndex, toIndex).clear()
}

fun <T> MutableList<T>.addBefore(ref: T?, item: T) {
	val pos = indexOf(ref)
	if (pos >= 0) add(pos, item)
	else add(item)
}

fun <T> MutableList<T>.addAfter(ref: T?, item: T) {
	val pos = indexOf(ref)
	add(pos + 1, item)
}

/**
 * If [replace] returns non-null value, replace old value with it
 */
inline fun<T> MutableList<T>.replaceSome(replace:(T)->T?) {
	for ((i,el) in withIndex()) {
		val value = replace(el)
		if (value != null) set(i, value)
	}
}

inline fun <T> Iterable<T>.maxOf(initial: Double = Double.MIN_VALUE, value: (T) -> Double): Double {
	return fold(initial) { r, e ->
		maxOf(r, value(e))
	}
}

inline fun <T> Iterable<T>.minOf(initial: Double = Double.MAX_VALUE, value: (T) -> Double): Double {
	return fold(initial) { r, e ->
		minOf(r, value(e))
	}
}

fun <T> Iterable<T>.indexOfOrNull(element: T): Int? = indexOf(element).takeIf { it >= 0 }