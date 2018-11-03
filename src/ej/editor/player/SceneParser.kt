package ej.editor.player

import ej.editor.expr.Evaluated
import ej.editor.expr.Evaluator
import ej.editor.expr.SimpleEvaluator
import ej.editor.external.TagDecl
import ej.editor.external.TagDeclVisitor
import ej.editor.external.TagLib
import ej.editor.parser.AbstractSceneParser
import ej.utils.plusAssign
import ej.utils.trueFalse
import java.util.*

/*
 * Created by aimozg on 06.08.2018.
 * Confidential until published on GitHub
 */
open class SceneParser : AbstractSceneParser() {
	val rng = Random()
	var tagProcessor: (tag: String, output: String) -> String = { _, output -> output }
	var fnProcessor: (fn: String, source: String, output: String) -> String = { _, _, output -> output }
	var evaluator: Evaluator = SimpleEvaluator(emptyMap())
	var playerEvaluator: Evaluator? = null
	
	private inner class TagDeclVisitorImpl : TagDeclVisitor() {
		val buffer = StringBuilder()
		
		override fun executeTag(decl: TagDecl) {
			buffer.setLength(0)
			super.executeTag(decl)
		}
		
		override fun testCondition(decl: TagDecl, condition: String): Boolean {
			val z: Evaluated
			if (decl.context == TagDecl.Context.PLAYER) {
				z = (playerEvaluator ?: evaluator).parseAndEvaluate(condition)
			} else {
				z = evaluator.parseAndEvaluate(condition)
			}
			println("Condition $condition -> $z")
			return z.isTrue()
		}
		
		override fun testChance(chance: Double): Boolean {
			return rng.nextDouble() < chance
		}
		
		override fun emit(text: String) {
			buffer += text
		}
		
		override fun parseText(text: String): String {
			return this@SceneParser.parse(text)
		}
	}
	
	private val tagDeclVisitor = TagDeclVisitorImpl()
	
	override fun evaluateTag(tag: String): String {
		val decl = TagLib.tags[tag] ?: error("Unknown tag $tag")
		tagDeclVisitor.executeTag(decl)
		val out = tagDeclVisitor.buffer.toString()
		return tagProcessor(tag, out)
	}
	
	override fun evaluateExpression(expr: String): String {
		return evaluator.parseAndEvaluate(expr).coerceToString().stringValue
	}
	
	open fun maybeWrap(fmt: String, content: String): String? = when (fmt) {
		"i" -> "<i>" to "</i>"
		"b" -> "<b>" to "</b>"
		"u" -> "<u>" to "</u>"
		"say" -> "<i>&laquo;" to "&raquo;</i>"
		else -> null
	}?.let { (op, cl) -> op + parseIfDelayed(content).trim() + cl }
	
	override fun evaluateFunction(name: String, rawArgument: String, rawContent: List<String>): String =
			maybeWrap(name, rawContent.firstOrNull()?:"")
					?: fnProcessor(
						name,
						"[$name($rawArgument)]",
						when (name) {
							"if" -> if (rawContent.size !in 0..2) {
								error("if block has wrong number of arguments")
							} else {
								val eval = evaluator.parseAndEvaluate(rawArgument)
								val ridx = eval.isTrue().trueFalse(0, 1)
								val rslt = rawContent.getOrNull(ridx) ?: ""
								parse(rslt)
							}
							else -> error("Unknown function $name")
						})
	
	override val delayedEvaluation: Boolean = true
	
}