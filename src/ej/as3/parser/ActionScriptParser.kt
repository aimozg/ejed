package ej.as3.parser

import ej.as3.ast.*
import ej.editor.expr.ExpressionParser
import ej.utils.AbstractParser
import ej.utils.fromJsString
import org.intellij.lang.annotations.Language

/*
 * Created by aimozg on 10.12.2018.
 * Confidential until published on GitHub
 */
// TODO dynamic, get, set, final, override, static
// maybe: include
// very maybe: Vector<> stuff
// very maybe: XML
// very very maybe: namespaces, native, labels
open class ActionScriptParser : AbstractParser() {
	companion object {
		@Suppress("unused", "UNUSED_PARAMETER")
		private inline fun Context.trace(s: () -> String) {
			// println("[$pos] "+s())
		}
		
		@Language("RegExp")
		private const val REX_WHITESPACE = """[\s\xA0]++"""
		@Language("RegExp")
		private const val REX_LINECOMMENT = """//[^\n]*+"""
		@Language("RegExp")
		private const val REX_BLOCKCOMMENT = """/\*(?:[^*]++|\n|\*(?!/))*+\*++/"""
		@Language("RegExp")
		private const val REX_XMLCOMMENT = """<!--(?:[^-]|\n|-(?!->))*+-->"""
		
		private val LA_STRING = Regex("""^(?:'(?:[^'\\\n]|\\.)*'|"(?:[^"\\\n]|\\.)*")""")
		private val LA_ID = ExpressionParser.LA_ID
		private val LA_NUMBER = ExpressionParser.LA_FLOAT
		private val LA_OBJECT_KEY = Regex("""^[\w$]++""")
		private val LA_PACKAGE_PATH = Regex("""^(\w++)(\.\w++)*+""")
		private val LA_LONG_ID = LA_PACKAGE_PATH
		private val LA_PACKAGE_PATH_WILDCARDS = Regex("""^(\w++)(\.\w++)*+(\.\*)?""")
		
		private val LA_BINARY_OPERATOR = Regex("""^(?:={1,3}|!==?|&&|\|\||(?:<{1,2}|>{1,3}|[*/%+\-&^|])=?|[.,]|as\b|in\b|instanceof\b|is\b)""")
		private val LA_UNARY_OPERATOR = Regex("""^(?:\+\+?|--?|~|!|delete\b|typeof\b|void\b)""")
		private val LA_POSTFIX_OPERATOR = Regex("""^(?:\+\+|--)""")
		
		private val LA_VISIBILITY_OR_DECL = Regex("""^(?:public|private|protected|internal|class|interface|const|function|var)\b""")
		
		private val LAW_BREAK = word("break")
		private val LAW_CASE = word("case")
		private val LAW_CLASS = word("class")
		private val LAW_CONST = word("const")
		private val LAW_CONTINUE = word("continue")
		private val LAW_DEFAULT = word("default")
		private val LAW_DO = word("do")
		private val LAW_EACH = word("each")
		private val LAW_ELSE = word("else")
		private val LAW_EXTENDS = word("extends")
		private val LAW_FOR = word("for")
		private val LAW_FUNCTION = word("function")
		private val LAW_IF = word("if")
		private val LAW_IMPLEMENTS = word("implements")
		private val LAW_IMPORT = word("import")
		private val LAW_IN = word("in")
		private val LAW_INTERFACE = word("interface")
		private val LAW_INTERNAL = word("internal")
		private val LAW_NAMESPACE = word("namespace")
		private val LAW_NEW = word("new")
		private val LAW_PACKAGE = word("package")
		private val LAW_PRIVATE = word("private")
		private val LAW_PROTECTED = word("protected")
		private val LAW_PUBLIC = word("public")
		private val LAW_RETURN = word("return")
		private val LAW_SUPER = word("super")
		private val LAW_SWITCH = word("switch")
		private val LAW_THROW = word("throw")
		private val LAW_TRY = word("try")
		private val LAW_USE = word("use")
		private val LAW_VAR = word("var")
		private val LAW_WHILE = word("while")
		private val LAW_WITH = word("with")
		
		private fun word(word: String) = Regex("^$word\\b")
	}
	
	override val LA_WHITESPACE: Regex =
			Regex(
					"^(?:(?:$REX_WHITESPACE)|(?:$REX_LINECOMMENT)|(?:$REX_BLOCKCOMMENT)|(?:$REX_XMLCOMMENT))++"
			)
	
	private fun Context.parseImportDirective(): AS3Import {
		eatOrFail(LAW_IMPORT)
		eatWs()
		val fullname = eatOrFail(LA_PACKAGE_PATH_WILDCARDS).value
		eatWs()
		eatOrFail(';')
		return AS3Import(fullname)
	}
	
	private fun Context.parseUseNamespaceDirective(): AS3UseNamespace {
		eatOrFail(LAW_USE)
		eatWs()
		eatOrFail(LAW_NAMESPACE)
		eatWs()
		val namespace = eatOrFail(LA_PACKAGE_PATH).value
		eatWs()
		eatOrFail(';')
		return AS3UseNamespace(namespace)
	}
	
	/** Expects no whitespace before. Does not remove whitespace after */
	private fun Context.parseStatement(inSwitch: Boolean = false): AS3Statement {
		when {
			peek('{') -> {
				val block = AS3BlockStatement()
				parseBlock(block.items, allowClassDecl = false, allowVisibility = false)
				return block
			}
			eat(LAW_BREAK) -> parserError("Statement not supported")
			eat(LAW_CASE) -> if (inSwitch) {
				parserError("Statement not supported")
			} else {
				parserError("'case' outside of 'switch'")
			}
			eat(LAW_CONTINUE) -> parserError("Statement not supported")
			eat(LAW_DEFAULT) -> if (inSwitch) {
				parserError("Statement not supported")
			} else {
				parserError("'default' outside of 'switch'")
			}
			eat(LAW_DO) -> parserError("Statement not supported")
			eat(LAW_FOR) -> parserError("Statement not supported")
			eat(LAW_IF) -> {
				eatWs()
				eatOrFail('(')
				val condition = parseExpression()
				eatOrFail(')')
				eatWs()
				val thenStmt = parseStatement()
				eatWs()
				val stmt = AS3IfStatement(condition)
				stmt.thenStmt = thenStmt
				if (eat(LAW_ELSE)) {
					eatWs()
					stmt.elseStmt = parseStatement()
				}
				return stmt
			}
			eat(LAW_RETURN) -> {
				eatWs()
				val expr = if (eat(';') || peek('}')) {
					null
				} else {
					parseExpression()
				}
				return AS3ReturnStatement(expr)
			}
			eat(LAW_SUPER) -> parserError("Statement not supported")
			eat(LAW_SWITCH) -> parserError("Statement not supported")
			eat(LAW_THROW) -> parserError("Statement not supported")
			eat(LAW_TRY) -> parserError("Statement not supported")
			eat(LAW_WHILE) -> parserError("Statement not supported")
			eat(LAW_WITH) -> parserError("Statement not supported")
			eat(';') -> return AS3EmptyStatement
			else -> return parseExpression()
		}
	}
	
	private fun Context.parseBlock(block: MutableList<AS3Statement>,
	                               allowClassDecl: Boolean,
	                               allowVisibility: Boolean) {
		eatOrFail('{')
		eatWs()
		while (!eat('}')) {
			when {
				peek(LA_VISIBILITY_OR_DECL) -> {
					block += parseDeclaration(allowClassDecl, allowVisibility)
				}
				else -> {
					val stmt = parseStatement()
					trace { "$stmt" }
					block += stmt
					eatWs()
					eat(';')
				}
			}
			eatWs()
		}
	}
	
	private fun Context.parseStringLiteral(): String {
		return eatOrFail(LA_STRING, "String expected").value
	}
	
	/** Expects no whitespace. Guaranteed to eat whitespace after */
	private fun Context.postExpression(expr: AS3Expression, minPrio: Int): AS3Expression {
		var x = expr
		while (true) {
			when {
				minPrio > AS3Priority.PRIMARY -> return x
				
				eat('(') -> {
					val call = AS3CallExpr(x)
					if (!eat(')')) {
						while (true) {
							call.arguments += parseExpression(AS3Priority.NOT_COMMA)
							if (eat(')')) break
							eatOrFail(',', "Expected ',' or ')'")
						}
					}
					x = call
				}
				
				eat('[') -> {
					val index = parseExpression()
					eatOrFail(']')
					x = AS3AccessExpr(x, index)
				}
				
				peek(LA_POSTFIX_OPERATOR) -> {
					val op = match.value
					if (minPrio > AS3Priority.POSTFIX) return x
					eat(match.value)
					if (x is AS3BinaryOperation && AS3Priority.POSTFIX > AS3Priority.of(x.op)) {
						// a+b++  x=a+b  op=++  -> a+(b++)  because postfix > +
						// a.b++  x=a.b  op=++  -> (a+b)++  because postfix < .
						x = AS3BinaryOperation(x.left, x.op, AS3PostfixOperation(x.right, op))
					} else if (x is AS3UnaryOperation) {
						// !a++  x=!a  op=++  -> !(a++)  because postfix > unary
						x = AS3UnaryOperation(x.op, AS3PostfixOperation(x.expr, op))
					} else {
						x = AS3PostfixOperation(x, op)
					}
				}
				
				peek(LA_BINARY_OPERATOR) -> {
					val op = match.value
					val prio = AS3Priority.of(op)
					if (prio < minPrio) return x
					eat(match.value)
					val y: AS3Expression
					y = if (AS3Priority.isRightAssociative(op)) {
						// e.g. in a=b=c after a= we allow same-priority operator capture
						// so it'll yield (b=c) and result expr will be a=(b=c)
						parseExpression(prio)
					} else {
						// e.g. in a+b+c after a+ we forbid same-priority operator capture
						// so it'll yield (b) only and result expr will be  (a+b)
						// and +c will be captured on next iteration
						parseExpression(prio + 1)
					}
					if (x is AS3UnaryOperation && prio > AS3Priority.UNARY) {
						// !a+b  x=!a  y=b  op=+  -> (!a)+b  because + < unary
						// !a.b  x=!a  y=b  op=.  -> !(a.b)  because . > unary
						x = AS3UnaryOperation(x.op, AS3BinaryOperation(x.expr, op, y))
					} else {
						x = AS3BinaryOperation(x, op, y)
					}
				}
				
				peek('?') -> {
					if (minPrio > AS3Priority.CONDITIONAL) return x
					eat('?')
					val thenExpr = parseExpression(AS3Priority.CONDITIONAL)
					eatOrFail(':')
					val elseExpr = parseExpression(AS3Priority.CONDITIONAL)
					x = AS3ConditionalExpression(x, thenExpr, elseExpr)
				}
				
				else -> return x
			}
			eatWs()
		}
	}
	
	/** Guaranteed to eat whitespace before and after */
	private fun Context.parseExpression(minPrio: Int = AS3Priority.ANYTHING): AS3Expression {
		eatWs()
		val x: AS3Expression
		when {
			isEmpty() -> parserError("Unexpected end of input")
			eat("(") -> {
				val y = parseExpression()
				eatOrFail(")")
				x = AS3WrappedExpression(y)
			}
			eat("[") -> {
				x = AS3ArrayLiteral()
				if (!eat("]")) {
					while (true) {
						x.items += parseExpression(AS3Priority.NOT_COMMA)
						if (eat("]")) break
						eatOrFail(",", "Expected ',' or ']'")
					}
				}
			}
			eat("{") -> {
				x = AS3ObjectLiteral()
				if (!eat("}")) {
					while (true) {
						val k: String = when {
							peek('"') || peek('\'') -> parseStringLiteral().fromJsString()
							else -> eatOrFail(LA_OBJECT_KEY, "Expected object key").value
						}
						eatWs()
						eatOrFail(':')
						val v = parseExpression()
						x.items[k] = v
						if (eat("}")) break
					}
				}
			}
			eat(LAW_NEW) -> {
				val y = parseExpression(AS3Priority.PRIMARY + 1)
				x = AS3NewExpr(y)
			}
			eat(LA_STRING) || eat(LA_ID) || eat(LA_NUMBER) -> {
				x = AS3Literal(eaten)
			}
			eat(LA_UNARY_OPERATOR) -> {
				val op = match.value
				val y = parseExpression(AS3Priority.UNARY)
				x = AS3UnaryOperation(op, y)
			}
			else -> parserError("Not a start of expression: '${str[0]}'")
		}
		eatWs()
		return postExpression(x, minPrio)
	}
	
	private fun Context.parseClassDeclaration(visibility: AS3Declaration.Visibility): AS3Class {
		eatOrFail(LAW_CLASS)
		eatWs()
		val name = eatOrFail(LA_ID, "Expected id").value
		val klass = AS3Class(name)
		klass.visibility = visibility
		eatWs()
		while (!peek('{')) {
			when {
				eat(LAW_EXTENDS) -> {
					if (klass.superclass != null) parserError("Multiple 'extends'")
					eatWs()
					klass.superclass = eatOrFail(LA_LONG_ID, "Superclass name expected").value
					eatWs()
				}
				eat(LAW_IMPLEMENTS) -> {
					if (klass.interfaces.isNotEmpty()) parserError("Multiple 'implements'")
					eatWs()
					klass.interfaces += eatOrFail(LA_LONG_ID, "Interface name expected").value
					eatWs()
					while (eat(',')) {
						eatWs()
						klass.interfaces += eatOrFail(LA_LONG_ID, "Interface name expected").value
						eatWs()
					}
				}
				else -> parserError("Expected extends,implements,or '{'")
			}
		}
		parseBlock(klass.body.items, allowClassDecl = false, allowVisibility = true)
		return klass
	}
	
	private fun Context.parseInterfaceDeclaration(visibility: AS3Declaration.Visibility): AS3Interface {
		eatOrFail(LAW_INTERFACE)
		parserError("Not implemented parseInterfaceDeclaration")
	}
	
	private fun Context.parseVarDeclaration(visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED): AS3Var {
		val isConst: Boolean = when {
			eat(LAW_CONST) -> true
			eat(LAW_VAR) -> false
			else -> parserError("Expected var or const")
		}
		eatWs()
		val name = eatOrFail(LA_ID, "Identifier expected").value
		val decl = AS3Var(isConst, name)
		decl.visibility = visibility
		eatWs()
		if (eat(':')) {
			eatWs()
			decl.type = eatOrFail(LA_LONG_ID, "Type expected").value
			eatWs()
		}
		if (eat('=')) {
			eatWs()
			decl.initializer = parseExpression()
			eatWs()
		}
		eat(';')
		return decl
	}
	
	private fun Context.parseFunctionExpression(requireName: Boolean): AS3FunctionExpr {
		eatOrFail(LAW_FUNCTION)
		eatWs()
		val fname: String? = eaten(LA_ID)?.value
		if (fname == null && requireName) parserError("Expected function with name")
		val func = AS3FunctionExpr(fname)
		eatWs()
		eatOrFail('(')
		if (!eat(')')) {
			do {
				eatWs()
				val isRest: Boolean
				if (eat("...")) {
					isRest = true
					eatWs()
				} else {
					isRest = false
				}
				val pname = eatOrFail(LA_ID, "Parameter name").value
				eatWs()
				val ptype: String?
				if (eat(':')) {
					eatWs()
					ptype = eatOrFail(LA_LONG_ID, "Parameter type").value
					eatWs()
				} else {
					ptype = null
				}
				val pinit: AS3Expression?
				if (eat('=')) {
					pinit = parseExpression()
				} else {
					pinit = null
				}
				func.parameters += AS3Parameter(pname, ptype, pinit, isRest)
			} while (eat(','))
			eatOrFail(')')
		}
		eatWs()
		if (eat(':')) {
			func.returnType = eatOrFail(LA_LONG_ID, "Return type").value
			eatWs()
		}
		parseBlock(func.body.items, false, false)
		return func
	}
	
	private fun Context.parseFunctionDeclaration(visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED): AS3FunctionDeclaration {
		val body = parseFunctionExpression(requireName = true)
		val fd = AS3FunctionDeclaration(body)
		fd.visibility = visibility
		return fd
	}
	
	private fun Context.parseDeclaration(allowClass: Boolean, allowVisibility: Boolean): AS3Declaration {
		val visibility = when {
			!allowVisibility -> AS3Declaration.Visibility.UNSPECIFIED
			eat(LAW_PRIVATE) -> AS3Declaration.Visibility.PRIVATE
			eat(LAW_PROTECTED) -> AS3Declaration.Visibility.PROTECTED
			eat(LAW_PUBLIC) -> AS3Declaration.Visibility.PUBLIC
			eat(LAW_INTERNAL) -> AS3Declaration.Visibility.INTERNAL
			else -> AS3Declaration.Visibility.UNSPECIFIED
		}
		if (visibility != AS3Declaration.Visibility.UNSPECIFIED) eatWs()
		return when {
			allowClass && peek(LAW_CLASS) -> parseClassDeclaration(visibility)
			allowClass && peek(LAW_INTERFACE) -> parseInterfaceDeclaration(visibility)
			peek(LAW_VAR) || peek(LAW_CONST) -> parseVarDeclaration(visibility)
			peek(LAW_FUNCTION) -> parseFunctionDeclaration(visibility)
			else -> parserError("Expected declaration")
		}
	}
	
	private fun Context.parsePackageDefinition(): AS3Package {
		eatOrFail(LAW_PACKAGE)
		eatWs()
		val fullname = eatOrFail(LA_PACKAGE_PATH, "Package name expected").value
		val pkg = AS3Package(fullname)
		eatWs()
		eatOrFail("{")
		eatWs()
		while (!eat("}")) {
			when {
				peek(LAW_IMPORT) -> pkg.directives += parseImportDirective()
				peek(LAW_USE) -> pkg.directives += parseUseNamespaceDirective()
				peek(LA_VISIBILITY_OR_DECL) ->
					pkg.declarations += parseDeclaration(allowClass = true, allowVisibility = true)
				else -> parserError("Package body expected")
			}
			eatWs()
		}
		return pkg
	}
	
	private fun Context.parseFileContent(file: AS3File) {
		eatWs()
		if (peek(LAW_PACKAGE)) {
			file.packageDecl = parsePackageDefinition()
			eatWs()
		}
		if (!isEof()) parserError("EOF expected")
	}
	
	private fun Context.parseFile(): AS3File {
		val file = AS3File()
		parseFileContent(file)
		return file
	}
	
	fun parseFile(s: String): AS3File {
		return Context(s).parseFile()
	}
	
	fun parseExpression(s: String): AS3Expression {
		val c = Context(s)
		val x = c.parseExpression()
		if (!c.isEof()) c.parserError("EOF expected")
		return x
	}
	
	fun parseFunction(s: String): AS3FunctionDeclaration {
		val c = Context(s)
		val f = c.parseDeclaration(allowClass = false, allowVisibility = true) as AS3FunctionDeclaration
		c.eatWs()
		if (!c.isEof()) c.parserError("EOF expected")
		return f
	}
	
	fun parseWord(s: String): String? {
		val c = Context(s)
		c.eatWs()
		return c.eaten(LA_ID)?.value
	}
}
