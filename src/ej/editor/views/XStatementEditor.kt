package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.stretchOnFocus
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import tornadofx.*

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
			this += when (stmt) {
				null -> label("<nothing>")
				is XsTextNode -> StmtEditor.TextStmt(stmt)
				is XsDisplay -> StmtEditor.DisplayStmt(stmt)
				is XsSet -> StmtEditor.SetStmt(stmt)
				is XsOutput -> StmtEditor.OutputStmt(stmt)
				is XsMenu -> TODO("StmtEditor.MenuStmt(stmt)")
				is XsButton -> TODO("StmtEditor.ButtonStmt(stmt)")
				is XsButtonHint -> TODO("StmtEditor.ButtonStmt(stmt)")
				is XsNext -> TODO("StmtEditor.ButtonStmt(stmt)")
				is XsBattle -> TODO("StmtEditor.ButtonStmt(stmt)")
				
				is XlIf -> StmtEditor.IfStmt(stmt)
				is XlElse -> TODO("StmtEditor.ElseStmt(stmt)")
				is XlElseIf -> TODO("StmtEditor.ElseIfStmt(stmt)")
				is XlSwitch -> TODO("StmtEditor.SwitchStmt(stmt)")
				
				is MonsterData.MonsterDesc -> label("<Monster Description>")
				else -> label("<unknown ${stmt.javaClass}>")
			}
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

open class StmtEditor<T:XStatement> constructor(val stmt:T) : HBox() {
	init {
		addClass(Styles.xstmtEditor)
		hgrow = Priority.ALWAYS
	}
	class TextStmt(stmt:XsTextNode) : StmtEditor<XsTextNode>(stmt) {
		init {
			textarea {
				hgrow = Priority.ALWAYS
				text = stmt.content
				isWrapText = true
				stretchOnFocus(3)
			}
		}
	}
	class DisplayStmt(stmt:XsDisplay) : StmtEditor<XsDisplay>(stmt) {
		init {
			label("Display text: ")
			textfield(stmt.ref)
		}
	}
	class SetStmt(stmt:XsSet) : StmtEditor<XsSet>(stmt) {
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
	class OutputStmt(stmt:XsOutput) : StmtEditor<XsOutput>(stmt) {
		init {
			label("Evaluate and display:")
			textfield(stmt.expression) { hgrow = Priority.ALWAYS }
		}
	}
	class IfStmt(stmt:XlIf) : StmtEditor<XlIf>(stmt) {
		init {
			label("If condition ")
			textfield(stmt.test)
			label(" is true")
			// TODO else, elseif
		}
	}
}
