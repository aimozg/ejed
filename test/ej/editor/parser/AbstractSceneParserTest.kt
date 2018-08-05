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
		val formatters: Map<String, Pair<String, String>>,
		val functions: Map<String, ParserImpl.(String, List<String>) -> String>,
		override var delayedEvaluation: Boolean
) : AbstractSceneParser() {
	var tagCount = HashMap<String,Int>()
	
	override fun evaluateTag(tag: String): String {
		tagCount[tag] = (tagCount[tag]?:0)+1
		return tags[tag].toString()
	}
	
	override fun evaluateFormatter(fmt: String, content: String): String {
		val formatter = formatters[fmt] ?: return content
		return formatter.first + content.trim() + formatter.second
	}
	
	override fun evaluateFunction(name: String, rawArgument: String, rawContent: List<String>): String {
		val fn = functions[name] ?: return ""
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
				), formatters = mapOf(
				"b" to ("<b>" to "</b>"),
				"say" to ("<i>'" to "'</i>")
		), functions = mapOf(
				"if" to { expr, args ->
					val s = when {
						expr.contains("true") -> args[0]
						args.size == 2 -> args[1]
						else -> ""
					}
					if (delayedEvaluation) parse(s) else s
				}
		),delayedEvaluation = true
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
	fun testIf() {
		assertParser("[if(true)[if(false)[he]1|[his]2]3|[if(true)[he]4|[his]5]6]7",
		             "her237")
		assertEquals(1, parser.tagCount["his"]?:0)
		assertEquals(0, parser.tagCount["he"]?:0)
	}
	@Test
	fun testIf2() {
		parser.delayedEvaluation = false
		assertParser("[if(true)[if(false)[he]1|[his]2]3|[if(true)[he]4|[his]5]6]7",
		             "her237")
		assertEquals(2, parser.tagCount["his"]?:0)
		assertEquals(2, parser.tagCount["he"]?:0)
	}
}