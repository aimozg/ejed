package ej.as3.parser

import ej.as3.ast.*
import ej.utils.AbstractParser
import org.intellij.lang.annotations.Language

/*
 * Created by aimozg on 10.12.2018.
 * Confidential until published on GitHub
 */
open class ActionScriptParser : AbstractParser<AS3File>() {
	companion object {
		@Language("RegExp")
		private const val REX_WHITESPACE = """\s++"""
		@Language("RegExp")
		private const val REX_LINECOMMENT = """//[^\n]*+"""
		@Language("RegExp")
		private const val REX_BLOCKCOMMENT = """/\*(?:[^*]|\n|\*(?!/))*\*++/"""
		@Language("RegExp")
		private const val REX_XMLCOMMENT = """<!--(?:[^-]|\n|-(?!->))*-->"""
		
		private val LA_ID = Regex("""^\w[\w\d]*+""")
		private val LA_PACKAGE_PATH = Regex("""^(\w++)(\.\w++)*+""")
		private val LA_LONG_ID = LA_PACKAGE_PATH
		private val LA_PACKAGE_PATH_WILDCARDS = Regex("""^(\w++)(\.\w++)*+(\.\*)?""")
		private val LA_VISIBILITY_OR_DECL = Regex("""^(public|private|protected|internal|class|interface|const|function|var)\b""")
		private val LAW_CLASS = Regex("""^class\b""")
		private val LAW_CONST = Regex("""^const\b""")
		private val LAW_EXTENDS = Regex("""^extends\b""")
		private val LAW_FUNCTION = Regex("""^function\b""")
		private val LAW_IMPLEMENTS = Regex("""^implements\b""")
		private val LAW_IMPORT = Regex("""^import\b""")
		private val LAW_INTERFACE = Regex("""^interface\b""")
		private val LAW_INTERNAL = Regex("""^internal\b""")
		private val LAW_NAMESPACE = Regex("""^namespace\b""")
		private val LAW_PACKAGE = Regex("""^package\b""")
		private val LAW_PRIVATE = Regex("""^private\b""")
		private val LAW_PROTECTED = Regex("""^protected\b""")
		private val LAW_PUBLIC = Regex("""^public\b""")
		private val LAW_USE = Regex("""^use\b""")
		private val LAW_VAR = Regex("""^var\b""")
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
	
	private fun Context.parseStatement(): AS3Statement {
		parserError("Not implemented parseStatement")
	}
	
	private fun Context.parseExpression(): AS3Expression {
		parserError("Not implemented parseStatement")
	}
	
	private fun Context.parseClassDeclaration(visibility: AS3Declaration.Visibility): AS3Class {
		eatOrFail(LAW_CLASS)
		eatWs()
		val name = eatOrFail(LA_ID, "Expected id").value
		val klass = AS3Class(name)
		klass.visibility = visibility
		eatWs()
		while (!eat('{')) {
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
		eatWs()
		while (!eat('}')) {
			when {
				eat(LA_VISIBILITY_OR_DECL) -> {
					eatWs()
					klass.body += parseDeclaration(allowClass = false, allowVisibility = true)
				}
				else -> klass.body += parseStatement()
			}
			eatWs()
		}
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
	
	private fun Context.parseFunctionDeclaration(visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED): AS3Function {
		parserError("Not implemented parseFunctionDeclaration")
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
		}
		if (!isEof()) parserError("Package expected")
	}
	
	override fun Context.doParse(): AS3File {
		val file = AS3File()
		parseFileContent(file)
		return file
	}
	
}
