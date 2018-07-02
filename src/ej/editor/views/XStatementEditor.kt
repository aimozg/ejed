package ej.editor.views

import ej.editor.Styles
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.control.Label
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.Region
import javafx.scene.web.WebView
import tornadofx.*
import java.util.concurrent.atomic.AtomicReference

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

class TextEditorView : View("Text Editor") {
	lateinit var editor: XStatementEditor
	val contentProperty = SimpleObjectProperty<XContentContainer?>(null)
	override val root = vbox {
		prefWidth = 800.0
		prefHeight = 600.0
		editor = XStatementEditor(contentProperty.value)
		this += editor
	}
	init {
		contentProperty.onChange {
			editor.removeFromParent()
			editor = XStatementEditor(it)
			root += editor
		}
	}
}

class XStatementEditorContainer(stmt:XStatement) : XStatementEditor(stmt) {
	val currentStatementProperty = SimpleObjectProperty<XStatementEditor?>(null)
	init {
		currentStatementProperty.addListener { _, oldValue, newValue ->
			if (oldValue != newValue) {
				oldValue?.markSelected(false)
				newValue?.markSelected(true)
			}
		}
		sceneProperty().onChange {
			it?.focusOwnerProperty()?.onChange { focusOwner ->
				val newStmt = focusOwner?.findParentOfType(XStatementEditor::class)
				currentStatementProperty.value = newStmt
			}
		}
	}
}

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
		hgrow = Priority.ALWAYS
	}
	class ForText(stmt:XcStyledText) : StmtEditorBody<XcStyledText>(stmt) {
		init {
			/*
			textarea {
				hgrow = Priority.ALWAYS
				text = stmt.textContent // TODO runs
				isWrapText = true
				stretchOnFocus(3)
			}
			*/
			val wv = synchronized(Companion) {
				cachedWebView.getAndSet(null) ?: WebView().apply {
					sceneProperty().onChange {
						if (it == null) cachedWebView.compareAndSet(null, this)
					}
					engine.documentProperty().onChange {
						//println(engine.executeScript("document.head.outerHTML+' '+document.body.outerHTML"))
					}
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
				isDisabled = stmt.inobj.isNullOrBlank()
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
				TODO("This should not happen (encountered ${stmt.javaClass} $stmt)")

				is XsDisplay -> DisplayStmt(stmt)
				is XsSet -> SetStmt(stmt)
				is XsOutput -> OutputStmt(stmt)
				is XsMenu -> TODO("MenuStmt(stmt)")
				is XsButton -> TODO("ButtonStmt(stmt)")
				is XsButtonHint -> TODO("ButtonHintStmt(stmt)")
				is XsNext -> TODO("NextStmt(stmt)")
				is XsBattle -> TODO("BattleStmt(stmt)")
				
				is XlIf -> IfStmt(stmt)
				is XlElse -> TODO("ElseStmt(stmt)")
				is XlElseIf -> TODO("ElseIfStmt(stmt)")
				is XlSwitch -> TODO("SwitchStmt(stmt)")
				
				is XcLib -> Label("Text library ${stmt.name}")
				is XcStyledText -> ForText(stmt)
				
				is MonsterData.MonsterDesc -> Label("<Monster Description>")
				else -> Label("<unknown ${stmt.javaClass}>")
			}
		}
	}
}
