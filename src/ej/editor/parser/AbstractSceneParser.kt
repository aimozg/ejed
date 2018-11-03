package ej.editor.parser

import ej.editor.utils.AbstractParser
import ej.utils.plusAssign
import ej.utils.unescapeBackslashes

/*
 * Created by aimozg on 05.08.2018.
 * Confidential until published on GitHub
 */

private val REX_SPACEBARS = Regex("""[ \t]{2}""")
private val REX_SPACE_BEFORE_PUNCTUATION = Regex(""" ++(?=[,.!?])""")

abstract class AbstractSceneParser : AbstractParser<String>() {
	abstract fun evaluateTag(tag: String): String
	abstract fun evaluateFunction(name: String, rawArgument: String, rawContent:List<String>): String
	abstract fun evaluateExpression(expr: String): String
	abstract val delayedEvaluation:Boolean
	fun parseIfDelayed(s: String): String = if (delayedEvaluation) parse(s) else s
	open fun postprocess(s:StringBuilder):String {
		return s.replace(REX_SPACEBARS," ").replace(REX_SPACE_BEFORE_PUNCTUATION,"")
	}
	private fun Context.readFunctionBody(eval:Boolean):ArrayList<String> {
		val arguments = ArrayList<String>()
		while (true) {
			// push either source or evaluated
			arguments += readUntil(eval && !delayedEvaluation, charArrayOf(']', '|')).toString()
			if (eaten == "]") {
				break
			} else if (eaten != "|") {
				parserError("Expected | or ]")
			}
		}
		return arguments
	}
	
	/**
	 * Parses content until end of content or `until` is encountered.
	 * If eval = true, tags are evaluated, and parsed content is returned.
	 * If eval = false, no evaluation happens, source substring is returned
	 * In any case, `eaten` contains the `until` encountered or empty string
	 */
	fun Context.readUntil(eval: Boolean, until: CharArray): StringBuilder {
		val rslt = StringBuilder(source.length * 2 / 3 + 1)
		val start = pos
		loop@ while (true) when {
			isEmpty() -> {
				eat(0)
				break@loop
			}
			eat('\\') -> {
				rslt += eaten(1)
			}
			eat('[') -> when {
				eat('=') -> {
					val expr = eatenUntilAnyBackslashEscaped('\\', ']')
							?.unescapeBackslashes()
							?: parserError("Unterminated [=")
					if (eval) {
						rslt += evaluateExpression(expr)
					}
					if (!eat(']')) parserError("Unterminated [=")
				}
				eat("--") -> {
					eatUntil("--]")
					eat("--]")
				}
				eatUntilAnyBackslashEscaped('\\',']','(',':') -> {
					val def = eaten.unescapeBackslashes()
					when {
						eat(']') -> {
							if (eval) {
								rslt += evaluateTag(def)
							}
						}
						eat('(') -> {
							if (!eval) rslt += '('
							// '[' def '(' arguments ')' content1 { '|' content2 }* ']' -->
							// --> function(def,arguments,content...)
							// TODO complex expressions containing ( ) [ ] "" ''
							val expr = eatenUntilAnyBackslashEscaped('(',')','[',']','"','\'','\\')
							if (!eat(')') || expr == null) {
								parserError("Bad expression/function construct")
							}
							val arguments = readFunctionBody(eval && !delayedEvaluation)
							if (eval) {
								rslt += evaluateFunction(def, expr, arguments)
							}
						}
						eat(':') -> {
							// '[' def ':' content { '|' content2 }*']' --> formatter(def,content)
							val arguments = readFunctionBody(eval && !delayedEvaluation)
							if (eval) {
								rslt += evaluateFunction(def, "", arguments)
							}
						}
						isEmpty() -> {
							rslt += '['
							rslt += def
						}
						else -> parserError("Expected ] ( or :")
					}
				}
				else -> {
					// unterminated '['
					rslt += '['
					rslt += eatenAll()
				}
			}
			else -> {
				if (eatUntilAnyBackslashEscaped('\\','[',']','|')) {
					if (eval) {
						rslt += eaten.unescapeBackslashes()
					}
				} else {
					eaten = ""
				}
				for (c in until) {
					if (eat(c)) break@loop
				}
				if (eaten.isEmpty()) {
					// Not a UNTIL-breaker, not a command-starter, but still, a text-delimiter => is a non-until text delimiter => text
					eat(1)
					rslt += eaten
				}
			}
		}
		if (!eval) {
			rslt.setLength(0)
			if (eaten.isNotEmpty()) {
				// Ends with <until>
				rslt.append(source, start, pos-1)
			} else {
				rslt.append(source, start, pos)
			}
		}
		return rslt
	}
	
	override fun Context.doParse(): String {
		return postprocess(readUntil(true, charArrayOf()))
	}
	
}