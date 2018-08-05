package ej.editor.player

import ej.editor.parser.AbstractSceneParser
import ej.mod.TagLib

/*
 * Created by aimozg on 06.08.2018.
 * Confidential until published on GitHub
 */
class SceneParser : AbstractSceneParser() {
	var tagProcessor:(tag:String,output:String)->String = {_,output -> output}
	
	override fun evaluateTag(tag: String): String {
		val out = TagLib.tags[tag]?.sample ?: error("Unknown tag $tag")
		return tagProcessor(tag,out)
	}
	
	override fun evaluateFormatter(fmt: String, content: String): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override fun evaluateFunction(name: String, rawArgument: String, rawContent: List<String>): String {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
	
	override val delayedEvaluation: Boolean = true
	
}