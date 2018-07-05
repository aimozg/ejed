package ej.utils

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