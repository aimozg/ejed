package ej.editor.utils

import java.lang.reflect.Modifier

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

val XML_ESCAPABLE_TOKENS = Regex("[&<>]")
val XML_ATTR_ESCAPABLE_TOKENS = Regex("[&<>\"\t\r\n]")
val XML_ENTITIES = mapOf(
		"&" to "&amp;",
		"<" to "&lt;",
		">" to "&gt;",
		"\"" to "&quot;",
		"\t" to "&#x9;",
		"\r" to "&#xD;",
		"\n" to "&#xA;"
)

fun String.escapeXml() = XML_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}
fun String.escapeXmlAttr() = XML_ATTR_ESCAPABLE_TOKENS.replace(this) {
	XML_ENTITIES[it.value] ?: it.value
}

val java.lang.reflect.Field.isStatic get() = Modifier.isStatic(modifiers)

