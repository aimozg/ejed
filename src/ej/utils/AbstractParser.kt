package ej.utils


/*
 * Created by aimozg on 11.07.2018.
 * Confidential until published on GitHub
 */

abstract class AbstractParser {
	protected open val LA_WHITESPACE = Regex("""^[\h\s\v]++""")
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
		fun eatAll() = eat(str.length)
		fun eatenAll() = eaten(str.length)
		
		fun peek(prefix: String): Boolean {
			return str.startsWith(prefix)
		}
		fun eaten(prefix:String):String? {
			if (peek(prefix)) {
				return eaten(prefix.length)
			}
			return null
		}
		
		fun eat(prefix: String): Boolean {
			return eaten(prefix) != null
		}
		
		fun eatOrFail(prefix: String, cause: String = "'$prefix' expected"): String {
			return eaten(prefix) ?: parserError(cause)
		}
		
		fun peek(prefix: Char): Boolean {
			return str.startsWith(prefix)
		}
		fun eaten(prefix:Char):String? {
			if (peek(prefix)) {
				return eaten(1)
			}
			return null
		}
		
		fun eat(prefix: Char): Boolean {
			return eaten(prefix) != null
		}
		
		fun eatOrFail(prefix: Char, cause: String = "'$prefix' expected"): String {
			return eaten(prefix) ?: parserError(cause)
		}
		
		fun peek(rex: Regex): Boolean {
			match = rex.find(str) ?: return false
			return true
		}
		fun eaten(rex:Regex):MatchResult? {
			if (!peek(rex)) return null
			eat(match.value.length)
			return match
		}
		fun eat(rex:Regex):Boolean {
			return eaten(rex) != null
		}
		
		fun eatOrFail(prefix: Regex, cause: String = "/${prefix.pattern}/ expected"): MatchResult {
			return eaten(prefix) ?: parserError(cause)
		}
		fun eatenUntil(sub:String):String? {
			val i = str.indexOfOrNull(sub) ?: return null
			return eaten(i)
		}
		
		fun eatUntil(sub: String): Boolean {
			return eatenUntil(sub) != null
		}
		
		fun eatUntilOrFail(sub: String, cause: String = "...'$sub' expected"): String {
			return eatenUntil(sub) ?: parserError(cause)
		}
		fun eatenUntilAny(vararg delimiters:Char):String? {
			val i = str.indexOfAnyOrNull(delimiters) ?: return null
			return eaten(i)
		}
		fun eatUntilAny(vararg delimiters:Char):Boolean {
			return eatenUntilAny(*delimiters) != null
		}
		
		fun eatUntilAnyOrFail(vararg delimiters: Char, cause: String = "(${delimiters.joinToString("|")})"): String {
			return eatenUntilAny(*delimiters) ?: parserError(cause)
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
		fun isEof() = str.isEmpty()
		fun isEmpty() = str.isEmpty()
		fun isNotEmpty() = str.isNotEmpty()
		fun parserError(message: String): Nothing {
			val n = parserExceptionCaptureSize()
			if (source.length <= n) throw ParserException(source, pos, message)
			/*
			 * source_souce_source_source_source
			 *                 ^--pos
			 *            |<-- n -->|
			 * |<--skip-->|
			 * |<-- ---- source.length ---- -->|
			 *
			 * skip + n/2 = pos
			 */
			val skip = maxOf(0, pos - n / 2)
			val cut = source.substring(skip, minOf(skip + n, source.length))
			if (skip == 0) throw ParserException(cut, pos, message)
			throw ParserException("($skip+)$cut", pos, message)
		}
	}
	
	protected open fun parserExceptionCaptureSize(): Int = 80
}
class ParserException(val source: String, val pos:Int,message:String) :
		Exception("$message at $pos in ${source.replace(Regex("""[\t\r\n]"""), " ")}")