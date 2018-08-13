package ej.utils

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */
operator fun StringBuilder.plusAssign(str:CharSequence) {
	append(str)
}
operator fun StringBuilder.plusAssign(c:Char) {
	append(c)
}

fun String.appendIf(condition:Boolean,ifTrue:String,ifFalse:String=""):String =
		if (condition) this+ifTrue else this+ifFalse
fun String?.affix(prefix:String,suffix:String=""):String =
		if (this == null) "" else prefix+this+suffix
fun String?.affixNonEmpty(prefix:String,suffix:String=""):String =
		if (this.isNullOrEmpty()) "" else prefix+this+suffix

infix fun String?.ifEmpty(default:String): String =
		if (this == null || this.isEmpty()) default else this
@JvmName("ifEmptyNullable")
infix fun String?.ifEmpty(default:String?): String? =
		if (this == null || this.isEmpty()) default else this

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
private val JS_ESCAPABLE_TOKENS = Regex("""["'\r\n\t\\]""")
private val JS_ESCAPES = mapOf(
		"\"" to """\"""",
		"\'" to """\'""",
		"\r" to """\r""",
		"\n" to """\n""",
		"\t" to """\t""",
		"\\" to """\\"""
)

fun String.escapeJs() = JS_ESCAPABLE_TOKENS.replace(this) {
	JS_ESCAPES[it.value] ?: it.value
}


fun String.toJsString():String = "'${escapeJs()}'"

fun CharSequence.indexOfOrNull(char: Char, startIndex: Int = 0, ignoreCase: Boolean = false):Int? =
		this.indexOf(char, startIndex, ignoreCase).takeIf { it >= 0 }

fun CharSequence.indexOfOrNull(string: String, startIndex: Int = 0, ignoreCase: Boolean = false):Int? =
		this.indexOf(string, startIndex, ignoreCase).takeIf { it >= 0 }

fun CharSequence.indexOfAnyOrNull(chars: CharArray, startIndex: Int = 0, ignoreCase: Boolean = false):Int? =
		this.indexOfAny(chars, startIndex, ignoreCase).takeIf { it >= 0 }

fun CharSequence.indexOfAnyOrNull(strings: Collection<String>, startIndex: Int = 0, ignoreCase: Boolean = false):Int? =
		this.indexOfAny(strings, startIndex, ignoreCase).takeIf { it >= 0 }

fun String.unescapeBackslashes(specialMappings:Map<Char,Char> = mapOf('r' to '\r','n' to '\n','t' to '\t')):String {
	val sb = StringBuilder(length)
	var startIndex = 0
	while(true) {
		val i = indexOfOrNull('\\',startIndex) ?: break
		sb.append(this, startIndex, i)
		val nextch = getOrNull(i+1) ?: '\\'
		sb.append(specialMappings[nextch] ?: nextch)
		startIndex = i+2
	}
	sb.append(this, startIndex, length)
	return sb.toString()
}