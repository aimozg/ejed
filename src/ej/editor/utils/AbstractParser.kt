package ej.editor.utils

import ej.utils.indexOfAnyOrNull
import ej.utils.indexOfOrNull


/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

abstract class AbstractParser<O> {
	protected open val LA_WHITESPACE = Regex("""^\s++""")
	protected abstract fun Context.doParse():O
	fun parse(input:String):O = Context(input).doParse()
	inner class Context(val source:String) {
		var str:String = source
		val pos get() = maxOf(0,source.length - str.length)
		fun eatWs():Boolean {
			return eat(LA_WHITESPACE)
		}
		fun eat(n:Int):Boolean {
			eaten = str.substring(0, n)
			str = str.substring(n)
			return true
		}
		fun eaten(n:Int):String {
			eat(n)
			return eaten
		}
		fun eatch():Char? {
			return eaten(1).getOrNull(0)
		}
		fun eat(prefix:String):Boolean {
			return eaten(prefix) != null
		}
		fun eat(prefix:Char):Boolean {
			return eaten(prefix) != null
		}
		fun eatAll() = eat(str.length)
		fun eatenAll() = eaten(str.length)
		fun eaten(prefix:String):String? {
			if (str.startsWith(prefix)) {
				return eaten(prefix.length)
			}
			return null
		}
		fun eaten(prefix:Char):String? {
			if (str.startsWith(prefix)) {
				return eaten(1)
			}
			return null
		}
		fun eaten(rex:Regex):MatchResult? {
			match = rex.find(str) ?: return null
			eat(match.value.length)
			return match
		}
		fun eat(rex:Regex):Boolean {
			return eaten(rex) != null
		}
		fun eatUntil(sub:String):Boolean {
			return eatenUntil(sub) != null
		}
		fun eatenUntil(sub:String):String? {
			val i = str.indexOfOrNull(sub) ?: return null
			return eaten(i)
		}
		fun eatenUntilAny(vararg delimiters:Char):String? {
			val i = str.indexOfAnyOrNull(delimiters) ?: return null
			return eaten(i)
		}
		fun eatUntilAny(vararg delimiters:Char):Boolean {
			return eatenUntilAny(*delimiters) != null
		}
		
		/**
		 * [delimiters] must contain the '\\' too
		 * @return string until any of delimiters with \ preserved
		 */
		fun eatenUntilAnyBackslashEscaped(vararg delimiters:Char):String? {
			val first = eatenUntilAny(*delimiters) ?: return null
			val sb = StringBuilder(first)
			while (eat('\\')) {
				sb.append('\\')
				val escaped = eatch() ?: break
				sb.append(escaped)
				val next = eatenUntilAny(*delimiters) ?: break
				sb.append(next)
			}
			eaten = sb.toString()
			return eaten
		}

		/**
		 * [delimiters] must contain the '\\' too
		 * [eaten] contains \ preserved
		 */
		fun eatUntilAnyBackslashEscaped(vararg delimiters:Char):Boolean {
			return eatenUntilAnyBackslashEscaped(*delimiters) != null
		}
		fun uneat(what:String = eaten) {
			str = what + str
		}
		var eaten:String = ""
		var match:MatchResult = Regex(".*").matchEntire("")!!
		fun isEmpty() = str.isEmpty()
		fun isNotEmpty() = str.isNotEmpty()
		fun parserError(message:String):Nothing = throw ParserException(source,pos,message)
	}
}
class ParserException(val source: String, val pos:Int,message:String) :
		Exception("$message at $pos in $source")