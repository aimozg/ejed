package ej.editor.views

import ej.editor.stmts.StmtEditorBody
import ej.editor.stmts.manager
import ej.editor.utils.RichTextProcessor
import ej.mod.*
import javafx.scene.control.Label
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import tornadofx.*
import java.util.concurrent.atomic.AtomicReference

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

private val cachedEditor = AtomicReference<HtmlEditorLite?>(null)

class FlashTextProcessor : RichTextProcessor() {
	override fun testBegin(tag: String) = when(tag) {
		"div" -> renameTag("br")
		in FlashPermittedElements -> take()
		else -> skip()
	}
	var skippedSpans:Int = 0
	
	override fun testOpen(tag: String, attrs: StringBuilder,single:Boolean) =
			if (tag == "span" && attrs.isBlank()) {
				skippedSpans++
				skip()
			} else take()
	
	override fun testEnd(tag: String) = when(tag) {
		"span" -> if (skippedSpans > 0) {
			skippedSpans--
			skip()
		} else take()
		"div" -> skip()
		in FlashPermittedElements -> take()
		else -> skip()
	}
	
	override fun testAttr(tag: String, name: String, value: String) =
			when(tag) {
				in FlashPermittedElements ->
					if (name in FlashPermittedElements.getOrDefault(name,emptySet())) take()
					else skip()
				else -> skip()
			}
	companion object {
		private val FlashPermittedElements:Map<String,Set<String>> = mapOf(
				"a" to setOf("target","href"),
				"b" to emptySet(),
				"br" to emptySet(),
				"font" to setOf("color","face","size"),
				"i" to emptySet(),
				"img" to setOf("src","width","height","align","hspace","vspace","id","checkPolicyFile"),
				"li" to emptySet(),
				"p" to setOf("class","align"),
				"span" to setOf("class"),
				"textformat" to setOf("blockindent", "indent", "leading", "leftmargin", "rightmargin", "tabstops"),
				"u" to emptySet()
		)
	}
}
class StmtEditorBodies {
	
	class ForText(stmt:XcText) : StmtEditorBody<XcText>() {
		init {
			synchronized(Companion) {
				cachedEditor.getAndSet(null) ?: HtmlEditorLite().apply {
					hgrow = Priority.ALWAYS
					processor = FlashTextProcessor()
					sceneProperty().onChange {
						if (it == null) cachedEditor.compareAndSet(null, this)
					}
				}
			}.attachTo(this) {
				bindHtmlContentBidirectional(stmt.textProperty())
			}
		}
	}
	class SetStmt(stmt:XsSet) : StmtEditorBody<XsSet>() {
		init {
			label("Property ")
			textfield(stmt.varname)
			checkbox("in object") {
				isDisable = stmt.inobj.isNullOrBlank()
			}
			when (stmt.op) {
				null, "=", "assign" -> label("set to")
				"+", "+=", "add" -> label("add ")
				"-" -> label("subtract ")
				"*" -> label("multiply by ")
				"/" -> label("divide by ")
			}
			textfield(stmt.value)
		}
	}
	
	companion object {
		fun bodyFor(stmt: XStatement?): Region {
			return when (stmt) {
				null -> Label("<nothing>")

				is XsSet -> SetStmt(stmt)
				is XsMenu -> VBox().apply { label("TODO MenuStmt(stmt)") }
				is XsButton -> VBox().apply { label("TODO ButtonStmt(stmt)") }
				is XsButtonHint -> VBox().apply { label("TODO ButtonHintStmt(stmt)") }
				is XsNext -> VBox().apply { label("TODO NextStmt(stmt)") }
				is XsBattle -> VBox().apply { label("TODO BattleStmt(stmt)") }
				
				is XlElse -> VBox().apply { label("TODO ElseStmt(stmt)") }
				is XlElseIf -> VBox().apply { label("TODO ElseIfStmt(stmt)") }
				is XlSwitch -> VBox().apply { label("TODO SwitchStmt(stmt)") }
				
				is XcLib -> VBox().apply { label(stmt.nameProperty().stringBinding{"Text library $it"}) }
				is XcText -> ForText(stmt)
				
				is MonsterData.MonsterDesc -> VBox().apply { label("<Monster Description>") }
				else -> stmt.manager()?.editorBody(stmt) ?:
					VBox().apply { label("<unknown ${stmt.javaClass}>") }
			}
		}
	}
}
