package ej.editor.utils


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
		fun eat(prefix:String):Boolean {
			return eaten(prefix) != null
		}
		fun eaten(prefix:String):String? {
			if (str.startsWith(prefix)) {
				return eaten(prefix.length)
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
			val i = str.indexOf(sub)
			return if (i >= 0) eaten(i) else null
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