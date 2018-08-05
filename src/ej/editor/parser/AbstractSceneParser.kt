package ej.editor.parser

import ej.editor.utils.AbstractParser
import ej.utils.plusAssign

/*
 * Created by aimozg on 05.08.2018.
 * Confidential until published on GitHub
 */

private val LA_TEXT = Regex("""^[^\[\]|\\]++""")
private val LA_TAGSTART = Regex("""^[^\\\](:]+""")
private val LA_EXPRESSION = Regex("""^[^\\\])]+""")
private val LA_COMMENT = Regex("""^([^-]|-[^-]|--[^]]|--?$)*+""")
private val REX_SPACEBARS = Regex("""[ \t]{2}""")

abstract class AbstractSceneParser : AbstractParser<String>() {
	abstract fun evaluateTag(tag: String): String
	abstract fun evaluateFormatter(fmt: String, content: String): String
	abstract fun evaluateFunction(name: String, rawArgument: String, rawContent:List<String>): String
	abstract val delayedEvaluation:Boolean
	open fun postprocess(s:StringBuilder):String {
		return s.replace(REX_SPACEBARS," ")
	}
	
	private fun Context.parseFunction(def:StringBuilder,rslt:StringBuilder) {
		// '[' def '(' arguments ')' content1 { '|' content2 }* ']' -->
		// --> function(def,arguments,content...)
		// TODO complex expressions containing ( ) [ ] "" '' \\
		val expr = eaten(LA_EXPRESSION)?.value ?: ""
		if (!eat(')')) {
			parserError("Bad expression/function construct")
		}
		val arguments = ArrayList<String>()
		while (true) {
			// push either source or evaluated
			arguments += readUntil(!delayedEvaluation, charArrayOf(']', '|')).toString()
			if (eaten == "]") {
				break
			} else if (eaten != "|") {
				parserError("Expected | or ]")
			}
		}
		rslt += evaluateFunction(def.toString(), expr, arguments)
	}
	
	/**
	 * Parses content until end of content or `until` is encountered.
	 * If eval = true, tags are evaluated, and parsed content is returned.
	 * If eval = false, no evaluation happens, source substring is returned
	 * In any case, `eaten` contains the `until` encountered or empty string
	 */
	fun Context.readUntil(eval: Boolean, until: CharArray): StringBuilder {
		val rslt = StringBuilder(source.length * 2 / 3 + 1)
		loop@ while (true) when {
			isEmpty() -> {
				eat(0)
				break@loop
			}
			eat('\\') -> {
				if (!eval) rslt += '\\'
				rslt += eaten(1)
			}
			eat('[') -> when {
				eat("--") -> {
					if (eval) {
						eat(LA_COMMENT)
						eat("--]")
					} else {
						rslt += "[--"
						rslt += eaten(LA_COMMENT)?.value?:""
						rslt += eaten("--]")?:""
					}
				}
				else -> {
					if (eval) {
						val def = StringBuilder(10)
						while (true) {
							def += eaten(LA_TAGSTART)?.value ?: ""
							if (eat('\\')) def += eaten(1)
							else break
						}
						when {
							eat(']') -> {
								rslt += evaluateTag(def.toString())
							}
							eat('(') -> {
								parseFunction(def, rslt)
							}
							eat(':') -> {
								// '[' def ':' content ']' --> formatter(def,content)
								val content = readUntil(true, charArrayOf(']'))
								rslt += evaluateFormatter(def.toString(), content.toString())
							}
							isEmpty() -> {
								rslt += '['
								rslt += def
							}
							else -> parserError("Expected ] ( or :")
						}
					} else {
						rslt += '['
						rslt += readUntil(eval, charArrayOf(']'))
						rslt += eaten
					}
				}
			}
			eat(LA_TEXT) -> {
				rslt += eaten
			}
			else -> {
				for (c in until) {
					if (eat(c)) break@loop
				}
				// Not a UNTIL-breaker, not a command-starter, but still, a text-delimiter => is a non-until text delimiter => text
				eat(1)
				rslt += eaten
			}
		}
		return rslt
	}
	
	override fun Context.doParse(): String {
		return postprocess(readUntil(true, charArrayOf()))
	}
	
}