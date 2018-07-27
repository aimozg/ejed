package ej.editor.player

import com.sun.webkit.dom.HTMLElementImpl
import ej.editor.AModView
import ej.editor.Styles
import ej.editor.expr.Evaluated
import ej.editor.expr.Evaluator
import ej.editor.utils.escapeXml
import ej.editor.utils.onChangeAndNow
import ej.editor.views.ManagedWebView
import ej.mod.ModDataNode
import ej.mod.StoryStmt
import ej.mod.locate
import ej.mod.visit
import ej.utils.appendIf
import ej.utils.crop
import javafx.beans.property.SimpleBooleanProperty
import javafx.scene.control.Button
import javafx.scene.layout.Priority
import org.controlsfx.control.ToggleSwitch
import tornadofx.*

/*
 * Created by aimozg on 26.07.2018.
 * Confidential until published on GitHub
 */

class ModPreview : AModView("EJEd - mod preview"), PlayerInterface {
	class MainText : ManagedWebView() {
		override fun templatePartStyle(): String {
			return super.templatePartStyle()+"\n"+Styles.PREVIEW_STYLE
		}
		
		override fun doSetInnerHtml(mainElement: HTMLElementImpl, it: String) {
			super.doSetInnerHtml(mainElement, it)
			webView.engine.executeScript("window.scrollTo(0,window.document.body.scrollHeight)")
		}
	}
	
	val uiButtons = ArrayList<Button>()
	val playingVisitor = PlayingVisitor(this)
	override val evaluator: Evaluator = object:Evaluator() {
		override fun evalId(id: String): Evaluated {
			return Evaluated.NullValue
		}
		
	}
//	lateinit var mainText: TextFlow // TextFlow is not selectable
	lateinit var mainText: MainText
	
	
	fun clearOutput() {
		mainText.htmlContent = ""
	}
	
	
	fun play(stmt:StoryStmt) {
		outputContent("[scene: ${stmt.path}]",ContentType.CODE,false)
		stmt.visit(playingVisitor)
	}
	
	val showSkippedProperty = SimpleBooleanProperty(false)
	val showCodeProperty = SimpleBooleanProperty(false)
	val showCommentsProperty = SimpleBooleanProperty(false)
	
	override val root = borderpane {
		addClass(Styles.playerView)
		top = toolbar {
			label("Show:")
			ToggleSwitch("Skipped").attachTo(this) {
				addClass("nogap")
				selectedProperty().bindBidirectional(showSkippedProperty)
			}
			ToggleSwitch("Code").attachTo(this) {
				addClass("nogap")
				selectedProperty().bindBidirectional(showCodeProperty)
			}
			ToggleSwitch("Comments").attachTo(this) {
				addClass("nogap")
				selectedProperty().bindBidirectional(showCommentsProperty)
			}
			spacer(Priority.NEVER) {prefWidth=20.0}
			button("Clear") {
				action { clearOutput() }
			}
		}
		mainText = MainText()
		center = mainText
		bottom = gridpane {
			hgap = 5.0
			vgap = 5.0
			constraintsForColumn(0).hgrow = Priority.ALWAYS
			constraintsForColumn(6).hgrow = Priority.ALWAYS
			for (i in 1..3) row {
				hbox{
				}
				for (j in 1..5) {
					button {
						uiButtons += this
						text = "Button${uiButtons.size}"
						isDisable = true
						maxWidth = Double.MAX_VALUE
					}
				}
				hbox{
				}
			}
		}
		showSkippedProperty.onChangeAndNow { show ->
			mainText.toggleMainElementClass("noskip",show == false)
		}
		showCodeProperty.onChangeAndNow { show ->
			mainText.toggleMainElementClass("nocode",show == false)
		}
		showCommentsProperty.onChangeAndNow { show ->
			mainText.toggleMainElementClass("nocomment",show == false)
		}
	}
	
	/***********
	 * PlayerInterface impl
	 **********/
	
	private var skipCounter:Int = 0
	private var skipMax:Int = 200
	override fun outputContent(t: String, type: ContentType, skipped: Boolean) {
		if (t.isEmpty()) return
		var cc = "content-$type".appendIf(skipped," skipped").toLowerCase()
		val remaining = skipMax - skipCounter
		if (!skipped) {
			if (t.startsWith("[forward:")) cc += " forward"
			if (t.startsWith("[scene:")) cc += " scene"
			skipCounter = 0
			val s = "<span class='$cc'>$t</span>"
			mainText.htmlContent += s
		} else if (remaining > 0) {
			val s = t.crop(remaining,"…")
			skipCounter += s.length
			mainText.htmlContent += "<span class='$cc'>$s</span>"
		}
	}
	
	override fun doNext(from: StoryStmt, ref: String) {
		doMenu(from, listOf(ButtonDecl("Next", ref)))
	}
	
	override fun doMenu(from: StoryStmt, buttons: List<ButtonDecl>) {
		for ((i,uib) in uiButtons.withIndex()) {
			val b = buttons.getOrNull(i)
			uib.isVisible = b != null
			if (b != null) {
				uib.isDisable = !b.enabled
				uib.text = b.name
				val target = lookup(from, b.ref)
				if (target == null) {
					uib.isDisable = true
					runtimeError("Unresolvable reference ${b.ref} in <button> in ${from.path}")
				}  else {
					uib.action {
						play(target)
					}
				}
			}
		}
	}
	
	override fun doBattle(ref: String, options: String?) {
		TODO("doBattle")
	}
	
	override fun runtimeError(msg: String, at: ModDataNode?) {
		mainText.htmlContent += "<span class='content-error'>${msg.escapeXml()}</span>"
//		TODO("runtimeError")
	}
	
	override fun lookup(from: StoryStmt, ref: String): StoryStmt? {
		from.locate(ref)?.let {
			return it
		}
//		TODO("check builtins")
		return null
	}
	
	override val doSkipped: Boolean = true
	
}