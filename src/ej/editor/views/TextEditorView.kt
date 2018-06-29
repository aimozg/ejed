package ej.editor.views

import ej.editor.utils.stretchOnFocus
import ej.mod.*
import javafx.beans.property.SimpleObjectProperty
import javafx.scene.Parent
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
			fragment = XStatementFragment(it)
			root += fragment
		}
	}
}

class XStatementFragment(val stmt: XStatement?) : Fragment() {
	override val root: Pane = hbox {
		onDock()
		style {
			borderWidth += box(1.px)
//			borderColor += box(Color.RED)
		}
		vbox {
			button("=")
		}
		vbox {
			hgrow = Priority.SOMETIMES
			// Header
			when (stmt) {
				null -> {
					label("<nothing>")
				}
				is XsTextNode -> {
					textarea {
						text = stmt.content
						prefRowCount = 1
						isWrapText = true
						stretchOnFocus()
						
					}
				}
				else -> {
					label("<unknown ${stmt.javaClass}>")
				}
			}
			if (stmt is XContentContainer) {
				for (s in stmt.content) {
					this += XStatementFragment(s).root
				}
			}
		}
		vbox {
			button("X")
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
	override fun onBeforeShow(view: UIComponent) {
		val data = ModData.unmarshaller().unmarshal(StringReader("""<desc>
            |Diva appears to be a vampire, the fangs and wings kind of give it away.
            |She circles above and around you, waiting for an opening while she constantly screeches.
            |Huh, she might be more bat-like that you initially thought.
            |Girl certainly has quite the pair of lungs if nothing else.
            |Her red and black dress gives her quite the villainous look, while having some unfortunate consequences what with it having a skirt and she being a flyer.
            |Somehow, she manages to keep herself from having a rather intimate reunion with the walls despite not looking at where she is flying in favour of drooling at the sight of your neck.
            |Guess all that screeching has an actual purpose aside from annoying you.
            |
            |me.mp.id = <output>me.mp.id</output>, mod.name = <output>mod.name</output>, me.extra.biteCounter = <output>me.extra.biteCounter</output>.
            |<if test="${'$'}final">
            |    Looks like she got one hell of a power-up thanks to your generous blood donations. Masochism much, Champ?
            |    <if test="silly()">
            |        Git gud, scrub.
            |    </if>
            |</if>
			|</desc>""".trimMargin())) as MonsterData.MonsterDesc
		println(data.content.joinToString(){ "${it.javaClass} : $it"})
		(view as TextEditorView).contentProperty.value =
				data
	}
}
fun main(args: Array<String>) {
	launch<TextTestApp>(args)
}