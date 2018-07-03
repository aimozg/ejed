package ej.editor.views

import ej.editor.Styles
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import tornadofx.*
import java.util.concurrent.atomic.AtomicReference

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

open class XStatementEditor(val parentStmt: XStatement?, val stmt: XStatement?, val depth:Int) : HBox() {
	constructor(stmt:XStatement?) : this(null,stmt,0)
	
	val expandedProperty = SimpleObjectProperty<Boolean>(false)
	var expanded by expandedProperty
	
	fun markSelected(value:Boolean) {
		if (value) addClass(Styles.xstmtSelected)
		else removeClass(Styles.xstmtSelected)
	}
	init {
		addClass(Styles.xstmt)
		addClass("depth-$depth")
		vbox {
			/*if (parentStmt != null) {
				button("≡").addClass(Styles.smallButton)
			} else {
				addClass(Styles.smallButtonSpace)
			}*/
		}
		vbox {
			hgrow = Priority.SOMETIMES
			// Header
			this += StmtEditorBody.bodyFor(stmt)
			if (stmt is XContentContainer) {
				for (s in stmt.content) {
					this += XStatementEditor(stmt, s, (depth+1) % Styles.MAX_DEPTH)
				}
			}
		}
		vbox {
			if (parentStmt != null) {
				button("≡") {
					addClass(Styles.smallButton)
				} // TODO drag & reorder
				button("X").addClass(Styles.smallButton) // TODO remove
			} else {
				addClass(Styles.smallButtonSpace)
			}
		}
	}
	
}

private val cachedWebView = AtomicReference<WebView?>(null)
open class StmtEditorBody<T:XStatement> constructor(val stmt:T) : HBox() {
	init {
		addClass(Styles.xstmtEditor)
		alignment = Pos.BASELINE_LEFT
		hgrow = Priority.ALWAYS
	}
	class ForText(stmt:XcStyledText) : StmtEditorBody<XcStyledText>(stmt) {
		init {
			val wv = synchronized(Companion) {
				cachedWebView.getAndSet(null) ?: WebView().apply {
					sceneProperty().onChange {
						if (it == null) cachedWebView.compareAndSet(null, this)
					}
					/*
					engine.documentProperty().onChange {
						//println(engine.executeScript("document.head.outerHTML+' '+document.body.outerHTML"))
					}
					*/
				}
			}
			add(wv)
			wv.hgrow = Priority.ALWAYS
			//language=HTML
			wv.engine.loadContent("<html><head><style type='text/css'>*{font-family:'${Styles.FONT_FACE_TEXT}', 'serif'}</style></head><body contentEditable='true'>${stmt.htmlContent}</body></html>")
		}
	}
	class DisplayStmt(stmt:XsDisplay) : StmtEditorBody<XsDisplay>(stmt) {
		init {
			label("Display text: ")
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
			textfield(stmt.test)
			label(" is true")
			// TODO else, elseif
		}
	}
	
	companion object {
		fun bodyFor(stmt: XStatement?): Region {
			return when (stmt) {
				null -> Label("<nothing>")
				is XmlElementB,
				is XmlElementI,
				is XmlElementFont ->
				kotlin.error("This should not happen (encountered ${stmt.javaClass} $stmt)")

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
				
				is XcLib -> VBox().apply { label("Text library ${stmt.name}") }
				is XcStyledText -> ForText(stmt)
				
				is MonsterData.MonsterDesc -> VBox().apply { label("<Monster Description>") }
				else -> VBox().apply { label("<unknown ${stmt.javaClass}>") }
			}
		}
	}
}
