package ej.editor.expr

import ej.editor.utils.AbstractParser
import ej.editor.utils.ParserException
import ej.utils.affixNonEmpty
import ej.utils.toJsString

private val RX_FLOAT= Regex("""^[+\-]?(\d+(\.\d++)?|\.\d++)(e[+\-]?\d++)?$""")
private val RX_INT= Regex("""^[+\-]?(0x)?\d++$""")
private val RX_ID= Regex("""^[a-zA-Z_$][a-zA-Z_$0-9]*+$""")
private val LA_BLOCK_COMMENT= Regex("""^/\*([^*/]|\*[^/]|[^*]/)*\*++/""")
private val LA_FLOAT= Regex("""^[+\-]?(\d++(\.\d++)?|\.\d++)(e[+\-]?\d++)?""")
private val LA_INT= Regex("""^[+\-]?(0x)?\d++""")
private val LA_ID= Regex("""^[a-zA-Z_$][a-zA-Z_$0-9]*+""")
private val LA_OPERATOR = Regex("""^(>=?|<=?|!==?|={1,3}|\|\||&&|or|and|eq|neq?|[lg](te?|eq?)|[-+*/%])""")
private val LA_DOUBLE_QUOTED_STRING_CONTENT = Regex("""^[^"\\]++""")
private val LA_SINGLE_QUOTED_STRING_CONTENT = Regex("""^[^'\\]++""")

class ExpressionParser : AbstractParser<Expression>() {
	override fun Context.doParse(): Expression {
		return when {
			eat(RX_INT) -> IntLiteral(source.toInt())
			eat(RX_FLOAT) -> FloatLiteral(source.toDouble())
			eat(RX_ID) -> Identifier(source)
			else -> evalUntil("")
		}
	}
	private fun Context.evalUntil(until:String):Expression {
		val x = evalExpr()
		if (until.isNotEmpty() && str.startsWith(until) ||
				until.isEmpty() && str.isEmpty()) return x
		parserError("Operator"+until.affixNonEmpty(" or ")+" expected")
	}
	private fun Context.evalExpr(minPrio:Int=0):Expression {
		eatWs()
		val x:Expression
		when {
			isEmpty() -> parserError("Unexpected end of input")
			eat("(") -> {
				x = evalUntil(")")
				eat(")")
			}
			eat("[") -> {
				val list = ArrayList<Expression>()
				if (!eat("]")) {
					list.add(evalExpr())
					while(eat(",")) list.add(evalExpr())
					if (!eat("]")) parserError("Expected ',' or ']'")
				}
				return ListExpression(list)
			}
			eat("{") -> {
				val map = HashMap<String,Expression>()
				if (!eat("}")) {
					while(true) {
						eatWs()
						val key = when {
							eat("'") || eat("\"") ->
								evalStringLiteral(eaten)
							eat(LA_ID) -> eaten
							else -> parserError("Key expected")
						}
						eatWs()
						if (!eat(":")) parserError("Expected ':' after key")
						eatWs()
						val value = evalExpr()
						map[key] = value
						if (eat("}")) break
						if (!eat(",")) parserError("Expected ',' or '}'")
					}
				}
				x = ObjectExpression(map)
			}
			eat(LA_INT) -> x = IntLiteral(eaten.toInt())
			eat(LA_FLOAT) -> x = FloatLiteral(eaten.toDouble())
			eat(LA_ID) -> x = Identifier(eaten)
			eat("'") || eat("\"") -> {
				x = StringLiteral(evalStringLiteral(eaten).toJsString())
			}
			else -> parserError("Not a start of expression: '${str[0]}'")
		}
		return evalPostExpr(x, minPrio)
	}
	private fun Context.evalPostExpr(expr: Expression, minPrio: Int):Expression {
		var x = expr
		while(true) {
			eatWs()
			if (eat("()")) {
				x = CallExpression(x, emptyList())
			} else if (eat("(")) {
				val args = ArrayList<Expression>()
				while(true) {
					args.add(evalExpr())
					if (eat(")")) break
					if (!eat(",")) parserError("Expected ')' or ','")
				}
				x = CallExpression(x, args)
			} else if (eat(".")) {
				if (!eat(LA_ID)) parserError("Identifier expected")
				x = DotExpression(x, eaten)
			} else if (eat("[")) {
				val y = evalUntil("]")
				if (!eat("]")) error("Expected ']'")
				x = AccessExpression(x, y)
			} else if (eat("?")) {
				val y = evalUntil(":")
				if (!eat(":")) error("Expected ':'")
				val z = evalExpr()
				return ConditionalExpression(x,y,z)
			} else if (eat(LA_OPERATOR)) {
				val op = BinaryOperator.parse(eaten) ?: parserError("Unknown operator '$eaten'")
				if (op.priority > minPrio) {
					val y = evalExpr(op.priority)
					x = BinaryExpression(x,op,y)
				} else {
					uneat()
					break
				}
			} else break
		}
		eatWs()
		return x
	}
	private fun Context.evalStringLiteral(delim:String):String {
		val s = StringBuilder()
		val rex = if (delim == "'")
			LA_DOUBLE_QUOTED_STRING_CONTENT else
			LA_SINGLE_QUOTED_STRING_CONTENT
		while (true) {
			if (eat("\\")) {
				s.append(when(eaten(1)){
					"n" -> "\n"
					"t" -> "\t"
					"r" -> "\r"
					"'" -> "'"
					"\"" -> "\""
					else -> ""
				})
			} else if (eat(delim)) {
				break
			} else if (eat(rex)) {
				s.append(eaten)
			} else parserError("This should never happen")
		}
		return s.toString()
	}
}

fun parseExpression(src:String): Expression {
	return ExpressionParser().parse(src)
}
inline fun parseExpressionSafe(src:String, exceptionHandler:(ParserException)->Unit={
	if (src.isNotEmpty()) System.err.println(it.message)
}): Expression {
	return try {
		ExpressionParser().parse(src)
	} catch (e: ParserException) {
		exceptionHandler(e)
		InvalidExpression(src)
	}
}