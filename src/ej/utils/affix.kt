package ej.utils

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */

fun String?.affix(prefix:String,suffix:String):String =
		if (this == null) "" else prefix+this+suffix
fun String?.affixNonEmpty(prefix:String,suffix:String):String =
		if (this.isNullOrEmpty()) "" else prefix+this+suffix
