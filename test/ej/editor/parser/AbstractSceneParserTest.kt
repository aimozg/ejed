package ej.editor.parser

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/*
 * Created by aimozg on 05.08.2018.
 * Confidential until published on GitHub
 */

private class ParserImpl(
		val tags: Map<String, String>,
		val functions: Map<String, ParserImpl.(String, List<String>) -> String>,
		val exprEval: (String) -> String,
		override var delayedEvaluation: Boolean
) : AbstractSceneParser() {
	var tagCount = HashMap<String,Int>()
	val exprs = ArrayList<String>()
	
	override fun evaluateExpression(expr: String): String {
		exprs += expr
		return exprEval(expr)
	}
	
	override fun evaluateTag(tag: String): String {
		@Suppress("NAME_SHADOWING")
		val tag = tag.trim()
		tagCount[tag] = (tagCount[tag]?:0)+1
		return tags[tag]?:"Unknown tag [$tag]"
	}
	
	override fun evaluateFunction(name: String, rawArgument: String, rawContent: List<String>): String {
		@Suppress("NAME_SHADOWING")
		val name = name.trim()
		val fn = functions[name] ?: error("Unknown function [$name]")
		return fn(rawArgument, rawContent)
	}
	
}

class AbstractSceneParserTest {
	
	private lateinit var parser: ParserImpl
	
	@Before
	fun setUp() {
		parser = ParserImpl(
				tags = mapOf(
						"name" to "Evelyn",
						"he" to "she",
						"his" to "her",
						"weapon" to "railgun"
				),
				functions = mapOf(
						"b" to { expr, args ->
							if (expr.isNotBlank()) error("Expected no-arg in [b($expr)...]")
							"<b>" + parseIfDelayed(args[0]).trim() + "</b>"
						},
						"say" to { expr, args ->
							if (expr.isNotBlank()) error("Expected no-arg in [b($expr)...]")
							"<i>'" + parseIfDelayed(args[0]).trim() + "'</i>"
						},
						"if" to { expr, args ->
							parseIfDelayed(when {
								expr.contains("true") -> args[0]
								args.size == 2 -> args[1]
								else -> ""
							})
						}
				),
				exprEval = { expr -> expr.reversed() },
				delayedEvaluation = true
		)
	}
	
	fun assertParser(input:String,output:String,parser:AbstractSceneParser=this.parser) {
		assertEquals(output,parser.parse(input))
	}
	
	@Test
	fun testEverything() {
		assertParser("Fake [name] takes [his] [weapon] and says, [--TODO reword to avoid DMCA issues--] [say: Hasta la vista, [name]]",
		             "Fake Evelyn takes her railgun and says, <i>'Hasta la vista, Evelyn'</i>")
	}
	
	@Test
	fun testCaps() {
		assertParser("[his] [His] [hIs] [HIS]",
		             "her Her her HER")
	}
	@Test
	fun testIf() {
		assertParser("[if(true)[if(false)[he]1|[his]2]3|[if(true)[he]4|[his]5]6]7",
		             "her237")
		assertEquals(1, parser.tagCount["his"]?:0)
		assertEquals(0, parser.tagCount["he"]?:0)
		assertParser("[if (true) [if (false) [he] 1 | [his] 2 ] 3 | [if (true) [he] 4 | [his] 5 ] 6 ] 7",
		             " her 2 3 7")
	}
	@Test
	fun testIfDE() {
		parser.delayedEvaluation = false
		assertParser("[if(true)[if(false)[he]1|[his]2]3|[if(true)[he]4|[his]5]6]7",
		             "her237")
		assertEquals(2, parser.tagCount["his"]?:0)
		assertEquals(2, parser.tagCount["he"]?:0)
	}
	
	@Test
	fun testEvalExpr() {
		assertParser("[if(true)[=if(true)]|[=false]]",
		             ")eurt(fi")
		assertEquals(1, parser.exprs.size)
		assertEquals("if(true)", parser.exprs[0])
	}
	
	@Test
	fun testEvalExprDE() {
		parser.delayedEvaluation = false
		assertParser("[if(true)[=if(true)]|[=false]]",
		             ")eurt(fi")
		assertEquals(2, parser.exprs.size)
		assertEquals("if(true)", parser.exprs[0])
		assertEquals("false", parser.exprs[1])
	}
	//@Test TODO implement short if
	fun testShortIf() {
		assertParser("[if true:[if false:[he]1|[his]2]3|[if true:[he]4|[his]5]6]7",
		             "her237")
		assertParser("[if true: [if false: [he]1 | [his]2 ]3 | [if true: [he]4 | [his]5]6]7",
		             "her237")
	}
}