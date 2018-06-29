package ej.editor.utils

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

val XML_ESCAPABLE_TOKENS = Regex("[&<>]")
val XML_ATTR_ESCAPABLE_TOKENS = Regex("[&<>\"\t\r\n]")
val XML_ENTITIES = mapOf(
		"&" to "&amp;",
		"<" to "&lt;",
		">" to "&gt;"
)
		/*
add('&', "&amp;", false)
add('<', "&lt;", false)
add('>', "&gt;", false)
add('"', "&quot;", true)
add('\t', "&#x9;", true)
add('\r', "&#xD;", false)
add('\n', "&#xA;", true)
*/
fun String.escapeXml() = XML_ESCAPABLE_TOKENS.replace(this) {
			XML_ENTITIES[it.value]?:it.value
		}