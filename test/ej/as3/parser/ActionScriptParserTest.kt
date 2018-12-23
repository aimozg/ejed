package ej.as3.parser

import ej.as3.ast.*
import ej.utils.ParserException
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import kotlin.test.assertFailsWith

/*
 * Created by aimozg on 15.12.2018.
 * Confidential until published on GitHub
 */
class ActionScriptParserTest {
	
	private val parser = ActionScriptParser()
	
	private fun parseExpression(s: String) = parser.parseExpression(s)
	private fun parseStatements(s: String) = parser.parseStatements(s)
	
	@Before
	fun setUp() {
		AS3Expression.EXPLICIT_PARENTHESES_IN_TOSTRING = true
	}
	
	private fun lit(src: String) = AS3Identifier(src)
	private val a = lit("a")
	private val b = lit("b")
	private val c = lit("c")
	private val d = lit("d")
	private val e = lit("e")
	private fun bop(left: AS3Expression, op: String, right: AS3Expression) = AS3BinaryOperation(left, op, right)
	private fun unary(op: String, x: AS3Expression) = AS3UnaryOperation(op, x)
	private fun postfix(x: AS3Expression, op: String) = AS3PostfixOperation(x, op)
	
	@Test
	fun testAssociativity() {
		// a.b.c is (a.b).c, not a.(b.c)
		assertEquals(
				bop(bop(a, ".", b), ".", c),
				parseExpression("a.b.c")
		)
		// a=b=c is a=(b=c), not (a=b)=c
		assertEquals(
				bop(a, "=", bop(b, "=", c)),
				parseExpression("a=b=c")
		)
		// a?b:c?d:e is a?b:(c?d:e), not (a?b:c)?d:e
		assertEquals(
				AS3ConditionalExpression(
						a, b, AS3ConditionalExpression(c, d, e)
				),
				parseExpression("a?b:c?d:e")
		)
		// !a+b  x=!a  y=b  op=+  -> (!a)+b  because + < unary
		assertEquals(
				bop(unary("!", a), "+", b),
				parseExpression("!a+b")
		)
		// !a.b  x=!a  y=b  op=.  -> !(a.b)  because . > unary
		assertEquals(
				unary("!", bop(a, ".", b)),
				parseExpression("!a.b")
		)
		// a+b++  x=a+b  op=++  -> a+(b++)  because postfix > +
		assertEquals(
				bop(a, "+", postfix(b, "++")),
				parseExpression("a+b++")
		)
		// a.b++  x=a.b  op=++  -> (a+b)++  because postfix < .
		assertEquals(
				postfix(bop(a, ".", b), "++"),
				parseExpression("a.b++")
		)
		// !a++  x=!a  op=++  -> !(a++)  because postfix > unary
		assertEquals(
				unary("!", postfix(a, "++")),
				parseExpression("!a++")
		)
	}
	
	@Test
	fun testIfs() {
		assertEquals(
				listOf(AS3IfStatement(lit("a"), lit("b"), lit("c"))),
				parseStatements("if(a) b else c")
		)
		assertEquals(
				listOf(AS3IfStatement(lit("a"), lit("b"), lit("c"))),
				parseStatements("if(a) b; else c")
		)
		assertFailsWith<ParserException> {
			parseStatements("if(a) b;; else c")
		}
	}
}