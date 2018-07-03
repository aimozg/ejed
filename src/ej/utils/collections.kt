package ej.utils

/*
 * Created by aimozg on 03.07.2018.
 * Confidential until published on GitHub
 */

fun<T:Any?> T.addToList(list:MutableList<T>):T {
	list.add(this)
	return this
}