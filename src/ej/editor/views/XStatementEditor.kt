package ej.editor.views

import ej.editor.Styles
import ej.mod.*
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
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
open class StmtEditorBody<T:XStatement> constructor(val stmt:T) : HBox() {
	init {
		addClass(Styles.xstmtEditor)
		alignment = Pos.BASELINE_LEFT
		hgrow = Priority.ALWAYS
	}
	class ForText(stmt:XcText) : StmtEditorBody<XcText>(stmt) {
		init {
			synchronized(Companion) {
				cachedEditor.getAndSet(null) ?: HtmlEditorLite().apply {
					sceneProperty().onChange {
						if (it == null) cachedEditor.compareAndSet(null, this)
					}
				}
			}.attachTo(this) {
				bindHtmlContentBidirectional(stmt.textProperty())
			}
		}
	}
	
	class DisplayStmt(stmt:XsDisplay) : StmtEditorBody<XsDisplay>(stmt) {
		init {
			label("Display subscene: ")
			textfield(stmt.ref)
		}
	}
	class SetStmt(stmt:XsSet) : StmtEditorBody<XsSet>(stmt) {
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
	class OutputStmt(stmt:XsOutput) : StmtEditorBody<XsOutput>(stmt) {
		init {
			label("Evaluate and display:")
			textfield(stmt.expression) { hgrow = Priority.ALWAYS }
		}
	}
	class IfStmt(stmt:XlIf) : StmtEditorBody<XlIf>(stmt) {
		init {
			label("If condition ")
			textfield(stmt.testProperty())
			label(" is true")
			// TODO else, elseif
		}
	}
	
	companion object {
		fun bodyFor(stmt: XStatement?): Region {
			return when (stmt) {
				null -> Label("<nothing>")

				is XsDisplay -> DisplayStmt(stmt)
				is XsSet -> SetStmt(stmt)
				is XsOutput -> OutputStmt(stmt)
				is XsMenu -> VBox().apply { label("TODO MenuStmt(stmt)") }
				is XsButton -> VBox().apply { label("TODO ButtonStmt(stmt)") }
				is XsButtonHint -> VBox().apply { label("TODO ButtonHintStmt(stmt)") }
				is XsNext -> VBox().apply { label("TODO NextStmt(stmt)") }
				is XsBattle -> VBox().apply { label("TODO BattleStmt(stmt)") }
				
				is XlIf -> IfStmt(stmt)
				is XlElse -> VBox().apply { label("TODO ElseStmt(stmt)") }
				is XlElseIf -> VBox().apply { label("TODO ElseIfStmt(stmt)") }
				is XlSwitch -> VBox().apply { label("TODO SwitchStmt(stmt)") }
				
				is XcLib -> VBox().apply { label(stmt.nameProperty().stringBinding{"Text library $it"}) }
				is XcText -> ForText(stmt)
				
				is MonsterData.MonsterDesc -> VBox().apply { label("<Monster Description>") }
				else -> VBox().apply { label("<unknown ${stmt.javaClass}>") }
			}
		}
	}
}
