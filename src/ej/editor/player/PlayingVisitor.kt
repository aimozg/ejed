package ej.editor.player

import ej.editor.expr.Evaluated
import ej.editor.expr.Expression
import ej.mod.*
import ej.utils.peek
import ej.utils.pop
import ej.utils.push

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */
class PlayingVisitor(val pif:PlayerInterface) : ModVisitor() {
	private var halting = false
	private var skipping = false
	private var menu:XsMenu? = null
	private val buttons = ArrayList<ButtonDecl>()
	private val stmtStack = ArrayList<XContentContainer>()
	private val storyStack = ArrayList<StoryStmt>()
	
	/*********************************************************
	 * complex stmts
 	 *********************************************************/
	
	override fun <T : ModDataNode> visitAllNodes(nodes: List<T>) {
		for (node in nodes) {
			if (halting) {
				halting = false
				break
			}
			node.visit(this)
		}
	}
	
	override fun visitAnyContentContainer(x: XContentContainer) {
		if (x is StoryStmt) {
			halting = false
			storyStack.push(x)
		}
		stmtStack.push(x)
		super.visitAnyContentContainer(x)
		stmtStack.pop().let { y ->
			if (y != x) pif.runtimeError("Statement stack corrupted, got $y",x)
		}
		if (x is StoryStmt) {
			halting = false
			storyStack.pop().let { y ->
				if (y != x) pif.runtimeError("Story stack corrupted, got $y", x)
			}
		}
	}
	
	override fun <T : XStatement> visitAllStatements(stmts: MutableList<T>) {
		for (stmt in stmts) {
			if (halting) {
				halting = false
				break
			}
			stmt.visit(this)
		}
	}
	
	/************************************************************
	 * flow control
	 ************************************************************/
	
	inline fun evaluateAndTake(node: ModDataNode, expr: Expression, body: (Evaluated) -> Unit) {
		val test = pif.evaluator.evaluate(expr)
		if (test is Evaluated.ErrorValue) {
			pif.runtimeError(test.msg,node)
		} else {
			body(test)
		}
	}
	
	private fun output(type: ContentType, content: String) {
		output(type) { content }
	}
	
	private inline fun output(type: ContentType, content: () -> String) {
		if (!skipping || pif.doSkipped) pif.outputContent(content(), type, skipping)
	}
	
	private inline fun outputCode(source: () -> String) {
		output(ContentType.CODE, source)
	}
	
	override fun visitSet(x: XsSet) {
		outputCode { "[set ${x.inobj?.plus(".") ?: ""}${x.varname} ${x.op ?: "="} ${x.value}]" }
		super.visitSet(x)
	}
	
	override fun visitCommand(x: XsCommand) {
		outputCode { "[command ${x.value}]" }
		super.visitCommand(x)
	}
	
	override fun visitOutput(x: XsOutput) {
		outputCode { "[output ${x.value}]" }
		if (!skipping) {
			evaluateAndTake(x, x.valueExpression) { rslt ->
				output(ContentType.TEXT, rslt.coerceToString().stringValue)
			}
		}
		super.visitOutput(x)
	}
	
	override fun visitIf(x: XlIf) {
		outputCode { "[if ${x.testExpression.source}]" }
		if (skipping) {
			super.visitIf(x)
			return
		}
		evaluateAndTake(x, x.testExpression) { test ->
			if (test.isTrue()) {
				visitThen(x.thenGroup)
				skipping = true
			} else {
				skipping = true
				visitThen(x.thenGroup)
				skipping = false
			}
			visitAllStatements(x.elseifGroups)
			x.elseGroup?.let { visitElse(it) }
			skipping = false
		}
	}
	
	override fun visitElseif(x: XlElseIf) {
		outputCode { "[elseif ${x.testExpression.source}]" }
		if (skipping) {
			super.visitElseif(x)
			return
		}
		evaluateAndTake(x, x.testExpression) { test ->
			if (test.isTrue()) {
				visitAllStatements(x.content)
				skipping = true
			} else {
				skipping = true
				visitAllStatements(x.content)
				skipping = false
			}
		}
	}
	
	override fun visitElse(x: XlElse) {
		outputCode { "[else]" }
		if (skipping) {
			super.visitElse(x)
			return
		}
		visitAllStatements(x.content)
	}
	
	override fun visitSwitch(x: XlSwitch) {
		if (skipping) {
			super.visitSwitch(x)
			return
		}
		TODO("switch")
	}
	
	override fun visitSwitchCase(x: XlSwitchCase) {
		if (skipping) {
			super.visitSwitchCase(x)
			return
		}
		TODO("switch case")
	}
	
	override fun visitSwitchDefault(x: XlSwitchDefault) {
		if (skipping) {
			super.visitSwitchDefault(x)
			return
		}
		TODO("switch default")
	}
	
	/************************************************************
	 * pif functions
	 ************************************************************/
	
	
	
	override fun visitText(x: XcText) {
		output(ContentType.TEXT, x.text)
		super.visitText(x)
		if (skipping) {
			return
		}
	}
	
	override fun visitComment(x: XlComment) {
		output(ContentType.COMMENT, "[-- ${x.text} --]")
		super.visitComment(x)
	}
	
	override fun visitMenu(x: XsMenu) {
		if (skipping) {
			if (pif.doSkipped) super.visitMenu(x)
			return
		}
		if (menu != null) pif.runtimeError("Nested <menu>",x)
		menu = x
		buttons.clear()
		super.visitMenu(x)
		menu = null
		pif.doMenu(storyStack.peek(), buttons)
	}
	
	override fun visitButton(x: XsButton) {
		outputCode { "[${x.text} -> ${x.ref}]" }
		if (skipping) {
			return
		}
		if (menu != null) {
			buttons += ButtonDecl(x.text,x.ref,!x.disabled)
			x.hint?.let {
				TODO("Button hint")
			}
		} else pif.runtimeError("<button> outside <menu>",x)
	}
	
	override fun visitForward(x: XsForward) {
		outputCode { "[forward: ${x.ref}]" }
		if (skipping) {
			return
		}
		val dest = pif.lookup(storyStack.peek(), x.ref)
		if (dest == null) {
			pif.runtimeError("Story not found: ${x.ref}",x)
		} else {
			dest.visit(this)
			halting = true
		}
	}
	
	override fun visitDisplay(x: XsDisplay) {
		outputCode { "[display: ${x.ref}]" }
		if (skipping) {
			return
		}
		val dest = pif.lookup(storyStack.peek(), x.ref)
		if (dest == null) {
			pif.runtimeError("Story not found: ${x.ref}",x)
		} else {
			dest.visit(this)
		}
	}
	
	override fun visitNext(x: XsNext) {
		outputCode { "[Continue -> ${x.ref}]" }
		if (skipping) {
			return
		}
		pif.doNext(storyStack.peek(), x.ref)
	}
	
	override fun visitBattle(x: XsBattle) {
		outputCode { "[Battle with ${x.monster}]" }
		if (skipping) {
			return
		}
		pif.doBattle(x.monster, x.options)
	}
	
	/**************************
	 * Should not encounter these nodes
	 **************************/
	
	override fun visitAnyStory(x: StoryStmt) {
	}
	override fun visitTrigger(x: SceneTrigger) {
	}

	override fun visitMod(x: ModData) {
		pif.runtimeError("PlayingVisitor called upon wrong node", x)
	}
	
	override fun visitMonster(x: MonsterData) {
		pif.runtimeError("PlayingVisitor called upon wrong node", x)
	}
	
	override fun <T : StoryStmt> visitAllStories(stmts: MutableList<T>) {
		pif.runtimeError("PlayingVisitor called upon wrong node (visitAllStories)")
	}
	
	override fun visitAllMonsters(monsters: MutableList<MonsterData>) {
		pif.runtimeError("PlayingVisitor called upon wrong node (visitAllMonsters)")
	}
}