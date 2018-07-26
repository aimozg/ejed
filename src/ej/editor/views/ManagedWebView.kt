package ej.editor.views

import com.sun.webkit.dom.HTMLElementImpl
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.SetChangeListener
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import org.w3c.dom.Document
import tornadofx.*

/*
 * Created by aimozg on 27.07.2018.
 * Confidential until published on GitHub
 */

open class ManagedWebView : VBox() {
	protected val htmlContentProperty = SimpleStringProperty("")
	
	var htmlContent:String by htmlContentProperty
	var htmlContentBidi: ObjectProperty<String>? = null
		set(value) {
			field?.unbindBidirectional(htmlContentProperty)
			value?.bindBidirectional(htmlContentProperty)
			field = value
		}
	
	fun bindHtmlContentBidirectional(prop: ObjectProperty<String>) {
		htmlContentBidi = null
		htmlContent = prop.value
		htmlContentBidi = prop
	}
	
	val mainElement: HTMLElementImpl?
		get() = mainElementIn(webView.engine.document)
	protected var modifyingHc = 0
	protected fun mainElementIn(document: Document?): HTMLElementImpl? =
		document?.getElementById("mainElement") as? HTMLElementImpl
	protected open fun documentPropertyChanged(doc: Document?) {
		mainElementIn(doc)?.innerHTML = htmlContent
	}
	protected open fun templatePartStyle() = "#mainElement{" +
			/**/"min-height:calc(99vh - 16px - 1em);" + // 8px body padding 1em p margin top
			"}"
	protected open fun templatePartMainElement() = "<div id='mainElement'></div>"
	protected open fun template() = "<!DOCTYPE html><html><head>" +
			"<style type='text/css'>${templatePartStyle()}</style>" +
			"</head><body>${templatePartMainElement()}</body></html>"
	
	val webView = WebView().attachTo(this) {
		engine.documentProperty().onChange { documentPropertyChanged(it) }
		hgrow = Priority.ALWAYS
		engine.loadContent(template())
		htmlContentProperty.onChange {
			if(modifyingHc==0) {
				val me = mainElement
				if (me != null) doSetInnerHtml(me, it ?: "")
			}
		}
	}
	
	protected open fun doSetInnerHtml(mainElement:HTMLElementImpl, it: String) {
		mainElement.innerHTML = it
	}
	
	val mainElementClasses = HashSet<String>().observable()
	fun toggleMainElementClass(className:String,toggle:Boolean) {
		if (toggle) mainElementClasses += className
		else mainElementClasses -= className
	}
	
	init {
		mainElementClasses.addListener { change: SetChangeListener.Change<out String> ->
			mainElement?.apply {
				var cn = className ?: ""
				change.elementAdded?.let { e ->
					cn = "$cn $e".trim()
				}
				change.elementRemoved?.let { e ->
					cn = cn.replace(Regex("""\b$e\b"""),"").trim()
				}
				className = cn
			}
		}
	}
}