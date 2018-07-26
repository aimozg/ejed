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
	
	protected inline fun evaluateAndTake(node:ModDataNode,expr:Expression,body:(Evaluated)->Unit) {
		val test = pif.evaluator.evaluate(expr)
		if (test is Evaluated.ErrorValue) {
			pif.runtimeError(test.msg,node)
		} else {
			body(test)
		}
	}
	
	override fun visitIf(x: XlIf) {
		if (!skipping || pif.doSkipped) pif.outputContent("[if ${x.testExpression.source}]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent("[elseif ${x.testExpression.source}]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent("[else]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent(x.text, ContentType.TEXT, skipping)
		if (skipping) {
			return
		}
	}
	
	override fun visitComment(x: XlComment) {
		if (!skipping || pif.doSkipped) pif.outputContent("[-- ${x.text} --]", ContentType.COMMENT, skipping)
		if (skipping) {
			return
		}
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
		if (!skipping || pif.doSkipped) pif.outputContent("[${x.text} -> ${x.ref}]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent("[forward: ${x.ref}]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent("[display: ${x.ref}]",ContentType.CODE,skipping)
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
		if (!skipping || pif.doSkipped) pif.outputContent("[Continue -> ${x.ref}]",ContentType.CODE,skipping)
		if (skipping) {
			return
		}
		pif.doNext(storyStack.peek(), x.ref)
	}
	
	override fun visitBattle(x: XsBattle) {
		if (!skipping || pif.doSkipped) pif.outputContent("[Battle with ${x.monster}]",ContentType.CODE,skipping)
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