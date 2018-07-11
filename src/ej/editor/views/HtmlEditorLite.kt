package ej.editor.views

import com.sun.webkit.dom.HTMLElementImpl
import ej.editor.Styles
import ej.editor.utils.XmlTextProcessor
import javafx.beans.property.ObjectProperty
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import tornadofx.*

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

open class HtmlEditorLite : VBox() {
	var htmlContent by property("")
//	fun htmlContentProperty() = getProperty(HtmlEditorLite::htmlContent)
	private val htmlContentProperty = getProperty(HtmlEditorLite::htmlContent)
	private var modifyingHc = 0
	var htmlContentBidi: ObjectProperty<String>? = null
		set(value) {
			field?.unbindBidirectional(htmlContentProperty)
			value?.bindBidirectional(htmlContentProperty)
			field = value
		}
	fun bindHtmlContentBidirectional(prop:ObjectProperty<String>) {
		htmlContentBidi = null
		htmlContent = prop.value
		htmlContentBidi = prop
	}
	var processor:XmlTextProcessor? = null
	
	@Suppress("unused")
	val webView = WebView().attachTo(this) {
		engine.documentProperty().onChange { doc ->
			val editable = doc?.getElementById("editable") as? HTMLElementImpl
			editable?.innerHTML = htmlContent
			editable?.addEventListener("input",{
				val hc = engine.executeScript("document.getElementById('editable').innerHTML") as? String ?: ""
				try {
					modifyingHc++
					htmlContent = processor?.parse(hc) ?: hc
				} finally {
					modifyingHc--
				}
			},false)
		}
		hgrow = Priority.ALWAYS
		//language=HTML
		engine.loadContent("<!DOCTYPE html><html><head><style type='text/css'>" +
				                   "#editable{" +
				                   /**/"min-height:calc(99vh - 16px - 1em);" + // 8px body padding 1em p margin top
				                   /**/"font-family:'${Styles.FONT_FACE_TEXT}', 'serif'}</style>" +
				                   "</head><body><p contentEditable='true' id='editable'></p></body></html>")
		htmlContentProperty.onChange {
			if(modifyingHc==0) {
				(engine.document?.getElementById("editable") as? HTMLElementImpl)?.innerHTML = it ?: ""
			}
		}
	}
	
}