package ej.editor.utils

import ej.utils.crop
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

abstract class RichTextProcessor {
	@JvmField
	protected val TAKE = 1
	@JvmField
	protected val SKIP = 2
	@JvmField
	protected val SKIP_TAG = 3
	// TODO MOD_TAG, MOD_ATTR_NAME, MOD_ATTR_VALUE
	fun process(source:String):String {
		val c = Context(source)
		val rslt = StringBuilder(source.length*2/3+1)
		while (c.isNotEmpty()) {
			rslt.append(c.eatUntil("<"))
			if (c.eat(LA_BEGIN) != null) {
				// <element
				val tbuf = StringBuilder(c.eaten)
				val tag = c.match.groupValues[1]
				var skip = takeBegin(tag) != TAKE
				while (true) {
					c.eatWs()
					if (c.eat(LA_ATTR) != null || c.eat(LA_FLAG) != null) {
						if (!skip) {
							when (takeAttr(tag, c.match.groupValues[1], c.match.groupValues[2])) {
								TAKE -> tbuf.append(c.eaten)
								SKIP_TAG -> skip = true
								SKIP -> {
								}
							}
						}
					} else if (c.eat(LA_OPEN_OR_SINGLE) != null) {
						tbuf.append(c.eaten)
						break
					} else {
						skip = true
						println("[WARN] Malformed XML near ${c.s.crop(20)}")
						c.eat(1)
					}
				}
				if (!skip) rslt.append(tbuf)
			} else if (c.eat(LA_END) != null) {
				// </element>
				if (takeEnd(c.match.groupValues[1]) == TAKE) {
					rslt.append(c.eaten)
				}
			} else if (c.isNotEmpty()) {
				c.eat(1)
			} else break
		}
		return rslt.toString()
	}
	abstract fun takeBegin(tag:String):Int
	abstract fun takeEnd(tag:String):Int
	abstract fun takeAttr(tag:String,name:String,value:String):Int
	class Context(var s:String) {
		fun eatWs():String {
			return eat(LA_WS)?.value?:""
		}
		fun eat(n:Int):String {
			eaten = s.substring(0,n)
			s = s.substring(n)
			return eaten
		}
		fun eat(prefix:String):String? {
			if (s.startsWith(prefix)) {
				return eat(prefix.length)
			}
			return null
		}
		fun eat(rex:Regex):MatchResult? {
			match = rex.find(s) ?: return null
			eat(match.value.length)
			return match
		}
		fun eatUntil(sub:String):String {
			val i = s.indexOf(sub)
			return if (i > 0) eat(i) else ""
		}
		var eaten:String = ""
		var match:MatchResult = Regex(".*").matchEntire("")!!
		fun isNotEmpty() = s.isNotEmpty()
	}
	companion object {
		
		private val LA_WS = Regex("""^\s++""")
		private val LA_BEGIN = Regex("""^<([^"<>&=\s]++)""") // 1 = tag name
		private val LA_ATTR = Regex("""^([^"<>&=\s]++)="?([^"<>]++)"?""") // 1 = name, 2 = value
		private val LA_FLAG = Regex("""^([^"<>&=\s]++)()(?!=)""") // 1 = name, 2 = empty
		private val LA_OPEN_OR_SINGLE = Regex("""^/?>""")
		private val LA_END = Regex("""^</([^"<>&=\s]++)>""") // 1 = tag name
	}
}