package ej.utils

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */

fun String?.affix(prefix:String,suffix:String):String =
		if (this == null) "" else prefix+this+suffix
fun String?.affixNonEmpty(prefix:String,suffix:String):String =
		if (this.isNullOrEmpty()) "" else prefix+this+suffix

fun String.crop(maxLength:Int, ellipsis:String="..."):String =
		if (length <= maxLength) this
		else if (maxLength < ellipsis.length) this.substring(0, maxLength)
		else this.substring(0, maxLength - ellipsis.length)+ellipsis

private val REGEX_WHITESPACE = Regex("\\s++")
/**
 * Trim and replace continuous whitespace with single space
 */
fun String.squeezeWs(): String {
	return replace(REGEX_WHITESPACE, " ").trim()
}