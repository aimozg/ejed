package ej.editor.player

import ej.editor.external.TagLib
import ej.editor.parser.AbstractSceneParser
import java.util.*

/*
 * Created by aimozg on 06.08.2018.
 * Confidential until published on GitHub
 */
open class SceneParser : AbstractSceneParser() {
	val rng = Random()
	var tagProcessor: (tag: String, output: String) -> String = { _, output -> output }
	var fnProcessor: (fn: String, source: String, output: String) -> String = { _, _, output -> output }
	var expressionEvaluator: (expr: String) -> Boolean = { rng.nextBoolean() }
	
	override fun evaluateTag(tag: String): String {
		val out = TagLib.tags[tag]?.sample ?: error("Unknown tag $tag")
		return tagProcessor(tag, out)
	}
	
	open fun maybeWrap(fmt: String, content: String): String? = when (fmt) {
		"i" -> "<i>" to "</i>"
		"b" -> "<b>" to "</b>"
		"u" -> "<u>" to "</u>"
		"say" -> "<i>&laquo;" to "&raquo;</i>"
		else -> null
	}?.let { (op, cl) -> op + parseIfNeeded(content).trim() + cl }
	
	override fun evaluateFunction(name: String, rawArgument: String, rawContent: List<String>): String =
			maybeWrap(name, rawContent.firstOrNull()?:"")
					?: fnProcessor(
						name,
						"[$name($rawArgument)]",
						when (name) {
							"if" -> if (rawContent.size !in 0..2) {
								error("if block has wrong number of arguments")
							} else {
								parse(rawContent.getOrNull(
										if (expressionEvaluator(rawArgument)) 0 else 1
								) ?: "")
							}
							else -> error("Unknown function $name")
						})
	
	override val delayedEvaluation: Boolean = true
	
}