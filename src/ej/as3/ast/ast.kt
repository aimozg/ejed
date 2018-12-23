package ej.as3.ast

import ej.utils.toJsString


/*
 * Created by aimozg on 10.12.2018.
 * Confidential until published on GitHub
 */
sealed class AS3Node {

}

class AS3Package(val fullname: String) : AS3Node() {
	val directives = ArrayList<AS3Directive>()
	val declarations = ArrayList<AS3Declaration>()
}

sealed class AS3Directive() : AS3Node()

class AS3Import(val fullname: String) : AS3Directive() {
	override fun toString() = "import $fullname;"
}

class AS3UseNamespace(val namespace: String) : AS3Directive() {
	override fun toString() = "use namespace $namespace;"
}

sealed class AS3Declaration() : AS3Statement() {
	abstract val name: String
	abstract val visibility: Visibility
	
	enum class Visibility {
		UNSPECIFIED, PRIVATE, INTERNAL, PROTECTED, PUBLIC;
		
		fun toCodePrefix(): String =
				if (this == UNSPECIFIED) ""
				else name.toLowerCase() + " "
	}
}

class AS3Class(override val name: String) : AS3Declaration() {
	var superclass: String? = null
	val interfaces = ArrayList<String>()
	val body = AS3BlockStatement()
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
	override fun toString() = buildString {
		append(visibility.toCodePrefix())
		append("class ", name)
		if (superclass != null) {
			append(" extends ", superclass)
		}
		if (interfaces.isNotEmpty()) {
			append(" implements ")
			interfaces.joinTo(this, separator = ", ")
		}
		append(" ", body)
	}
}

class AS3Interface(override val name: String) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
}

class AS3Var(val isConst: Boolean, override val name: String) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
	var type: String? = null
	var initializer: AS3Statement? = null
	
	override fun toString() = buildString {
		append(visibility.toCodePrefix())
		if (isConst) append("const ") else append("var ")
		append(name)
		if (type != null) append(": ", type)
		if (initializer != null) append(" = ", initializer)
		append(";")
	}
}

class AS3FunctionDeclaration(val fn: AS3FunctionExpr) : AS3Declaration() {
	override var visibility: AS3Declaration.Visibility = AS3Declaration.Visibility.UNSPECIFIED
	override val name = fn.name ?: error("Declaration must have a name")
	override fun toString() =
			visibility.toCodePrefix() + fn.toString()
}

sealed class AS3Statement : AS3Node()

object AS3EmptyStatement : AS3Statement() {
	override fun toString() = ";"
}

data class AS3BlockStatement(
		val items: ArrayList<AS3Statement> = ArrayList()
) : AS3Statement() {
	override fun toString() = if (items.isEmpty()) "{}" else "{ /* ${items.size} statements */ }"
}

data class AS3ReturnStatement(val expr: AS3Expression?) : AS3Statement() {
	override fun toString() = "return $expr;"
}

data class AS3IfStatement(
		val condition: AS3Expression,
		var thenStmt: AS3Statement = AS3EmptyStatement,
		var elseStmt: AS3Statement? = null
) : AS3Statement() {
	
	
	override fun toString() =
			"if ($condition) $thenStmt" + (elseStmt?.let { " else $it" } ?: "")
}

sealed class AS3Expression : AS3Statement() {
	open fun toSource() = toString()
	
	fun asSum(): List<AS3Expression> {
		if (this is AS3BinaryOperation && op == "+") return left.asSum() + right.asSum()
		else return listOf(this)
	}
	
	companion object {
		@JvmStatic
		protected fun wrapToString(s: String) =
				if (EXPLICIT_PARENTHESES_IN_TOSTRING) "($s)" else s
		
		var EXPLICIT_PARENTHESES_IN_TOSTRING = false
	}
}

data class AS3UnaryOperation(
		val op: String,
		val expr: AS3Expression
) : AS3Expression() {
	override fun toString() = wrapToString("$op$expr")
}

data class AS3PostfixOperation(
		val expr: AS3Expression,
		val op: String
) : AS3Expression() {
	override fun toString() = wrapToString("$expr$op")
}

data class AS3BinaryOperation(
		val left: AS3Expression,
		val op: String,
		val right: AS3Expression
) : AS3Expression() {
	override fun toString() = wrapToString(when (op) {
		                                       "." -> "$left.$right"
		                                       else -> "$left $op $right"
	                                       })
}

data class AS3ConditionalExpression(
		val ifExpr: AS3Expression,
		val thenExpr: AS3Expression,
		val elseExpr: AS3Expression
) : AS3Expression() {
	override fun toString() = wrapToString("$ifExpr ? $thenExpr : $elseExpr")
}

data class AS3WrappedExpression(
		val wrapped: AS3Expression
) : AS3Expression() {
	override fun toString() = "($wrapped)"
}

data class AS3AccessExpr(
		val obj: AS3Expression,
		val index: AS3Expression
) : AS3Expression() {
	override fun toString() = wrapToString("$obj[$index]")
}

data class AS3CallExpr(
		val func: AS3Expression,
		val arguments: ArrayList<AS3Expression> = ArrayList()
) : AS3Expression() {
	override fun toString() = wrapToString(
			func.toString() + arguments.joinToString(prefix = "(", separator = ", ", postfix = ")"))
}

data class AS3NewExpr(
		val konstructor: AS3Expression
) : AS3Expression() {
	override fun toString() = wrapToString("new $konstructor")
}

data class AS3NumberLiteral(
		val src: String
) : AS3Expression() {
	override fun toString() = src
}

data class AS3StringLiteral(
		val src: String
) : AS3Expression() {
	override fun toString() = src
}

data class AS3Identifier(
		val src: String
) : AS3Expression() {
	override fun toString() = src
}

data class AS3ArrayLiteral(
		val items: ArrayList<AS3Expression> = ArrayList()
) : AS3Expression() {
	
	override fun toString(): String = items.joinToString(prefix = "[", separator = ", ", postfix = "]")
}

data class AS3ObjectLiteral(
		val items: HashMap<String, AS3Expression> = HashMap()
) : AS3Expression() {
	override fun toString(): String =
			items.entries.joinToString(
					prefix = "{",
					separator = ", ",
					postfix = "}") { (k, v) ->
				"${k.toJsString()}: $v"
			}
}

data class AS3Parameter(
		val name: String,
		val type: String?,
		val defaultValue: AS3Expression?,
		val isRest: Boolean = false
) : AS3Node() {
	override fun toString() = buildString {
		if (isRest) append("...")
		append(name)
		if (type != null) append(": ", type)
		if (defaultValue != null) append(" = ", defaultValue)
	}
}

data class AS3FunctionExpr(
		val name: String?,
		var returnType: String? = null,
		val parameters: ArrayList<AS3Parameter> = ArrayList(),
		val body: AS3BlockStatement = AS3BlockStatement()
) : AS3Expression() {
	override fun toString() = buildString {
		append("function ")
		if (name != null) append(name, " ")
		append("(")
		parameters.joinTo(this, separator = ", ")
		append(") ")
		append(body)
	}
	
}

object AS3Operators {
	const val NOTHING = 999
	// Primary     [] {x:y} () f(x) new x.y x[y] <></> @ :: ..
	const val PRIMARY = 150
	// Postfix     x++ x--
	const val POSTFIX = 140
	// Unary       ++x --x + - ~ ! delete typeof void
	const val UNARY = 130
	// Multiplicative  * / %
	const val MULTIPLICATIVE = 120
	// Additive    + -
	const val ADDITIVE = 110
	// Bitwise shift   << >> >>>
	const val SHIFT = 100
	// Relational  < > <= >= as in instanceof is
	const val RELATIONAL = 90
	// Equality    == != === !==
	const val EQUALITY = 80
	// Bitwise AND &
	const val BITWISE_AND = 70
	// Bitwise XOR ^
	const val BITWISE_XOR = 60
	// Bitwise OR  |
	const val BITWISE_OR = 50
	// Logical AND &&
	const val LOGICAL_AND = 40
	// Logical OR  ||
	const val LOGICAL_OR = 30
	// Conditional ?:
	const val CONDITIONAL = 20
	// Assignment  = *= /= %= += -= <<= >>= >>>= &= ^= |=
	const val ASSIGNMENT = 10
	
	const val NOT_COMMA = 5
	// Comma       ,
	const val COMMA = 1
	
	const val ANYTHING = 0
	
	fun isAssignment(operator: String) = when (operator) {
		"=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", ">>>=", "&=", "^=", "|=" -> true
		else -> false
	}
	
	fun isRightAssociative(operator: String) = isAssignment(operator)
	fun of(operator: String): Int = when (operator) {
		"." -> PRIMARY
		"*", "/", "%" -> MULTIPLICATIVE
		"+", "-" -> ADDITIVE
		">>", "<<", ">>>" -> SHIFT
		"<", ">", "<=", ">=", "as", "in", "instanceof", "is" -> RELATIONAL
		"==", "!=", "===", "!==" -> EQUALITY
		"&" -> BITWISE_AND
		"^" -> BITWISE_XOR
		"|" -> BITWISE_OR
		"&&" -> LOGICAL_AND
		"||" -> LOGICAL_OR
		"=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", ">>>=", "&=", "^=", "|=" -> ASSIGNMENT
		"," -> COMMA
		else -> error("Unknown operator $operator")
	}
}

class AS3File : AS3Node() {
	var packageDecl: AS3Package? = null
	val outerDecls = ArrayList<AS3Declaration>()
}