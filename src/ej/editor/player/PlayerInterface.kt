package ej.editor.player

import ej.editor.expr.Evaluator
import ej.mod.ModDataNode
import ej.mod.StoryStmt

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */

enum class ContentType {
	COMMENT,
	CODE,
	TEXT
}

interface PlayerInterface {
	val evaluator: Evaluator
	val doSkipped: Boolean
	
	fun outputContent(t:String,type:ContentType,skipped:Boolean)
	fun doNext(from: StoryStmt, ref: String)
	fun doMenu(from: StoryStmt, buttons: List<ButtonDecl>)
	fun doBattle(ref:String, options:String?)
	fun lookup(from: StoryStmt, ref:String):StoryStmt?
	
	fun runtimeError(msg:String, at: ModDataNode?=null)
}
fun PlayerInterface.outputText(t:String) {
	outputContent(t,ContentType.TEXT,false)
}

class ButtonDecl(val name:String,
                 val ref:String,
                 val enabled:Boolean = true,
                 val hintHeader:String = "",
                 val hintText:String = "")