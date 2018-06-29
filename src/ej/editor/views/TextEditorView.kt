package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.stretchOnFocus
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.layout.Priority
import tornadofx.*
import java.io.StringReader

/*
 * Created by aimozg on 29.06.2018.
 * Confidential until published on GitHub
 */

class TextEditorView : View("Text Editor") {
	lateinit var fragment: XStatementFragment
	val contentProperty = SimpleObjectProperty<XContentContainer?>(null)
	override val root = vbox {
		prefWidth = 800.0
		prefHeight = 600.0
		fragment = XStatementFragment(contentProperty.value)
		this += fragment
	}
	init {
		contentProperty.onChange {
			fragment.removeFromParent()
			fragment = XStatementFragment( it)
			root += fragment
		}
	}
}

class XStatementFragment(val parentStmt: XStatement?, val stmt: XStatement?, val depth:Int) : Fragment() {
	constructor(stmt:XStatement?) : this(null,stmt,0)
	override val root: Pane = hbox {
		addClass(Styles.xstmt)
		addClass("depth-$depth")
		style {
			borderWidth += box(1.px)
		}
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
					this += XStatementFragment(stmt,s,(depth+1) % Styles.MAX_DEPTH).root
				}
			}
		}
		vbox {
			if (parentStmt != null) {
				button("≡").addClass(Styles.smallButton) // TODO drag & reorder
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

class XContentFragment(val stmt: XContentContainer?) : Fragment() {
	override val root: Parent = vbox {
			hgrow = Priority.SOMETIMES
			when (stmt) {
				null -> { label("<nothing>") }
				else -> {
					label("<unknown ${stmt.javaClass}>")
				}
			}
			
		}
}


class TextTestApp : App(TextEditorView::class) {
	init {
		importStylesheet(Styles::class)
	}
	override fun onBeforeShow(view: UIComponent) {
		val data = ModData.unmarshaller().unmarshal(StringReader(("" +
				"<desc>" +
				"Diva appears to be a vampire, the fangs and wings kind of give it away.\n" +
				"She circles above and around you, waiting for an opening while she constantly screeches.\n" +
				"Huh, she might be more bat-like that you initially thought.\n" +
				"Girl certainly has quite the pair of lungs if nothing else.\n" +
				"Her red and black dress gives her quite the villainous look, while having some unfortunate consequences what with it having a skirt and she being a flyer.\n" +
				"Somehow, she manages to keep herself from having a rather intimate reunion with the walls despite not looking at where she is flying in favour of drooling at the sight of your neck.\n" +
				"Guess all that screeching has an actual purpose aside from annoying you." +
				"<if test=\"\$final\">" +
				"\n\nLooks like she got one hell of a power-up thanks to your generous blood donations. Masochism much, Champ?" +
				"<if test=\"silly()\">" +
				"\nGit gud, scrub." +
				"</if>" +
				"</if>" +
				"</desc>").trimMargin())) as MonsterData.MonsterDesc
		println(data.content.joinToString{ "${it.javaClass} : $it"})
		(view as TextEditorView).contentProperty.value =
				data
	}
}
fun main(args: Array<String>) {
	launch<TextTestApp>(args)
}