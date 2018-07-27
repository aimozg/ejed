package ej.editor.views

import ej.editor.Styles
import ej.editor.utils.XmlTextProcessor
import org.w3c.dom.Document

/*
 * Created by aimozg on 04.07.2018.
 * Confidential until published on GitHub
 */

open class HtmlEditorLite : ManagedWebView() {
	var processor:XmlTextProcessor? = null
	
	override fun documentPropertyChanged(doc: Document?) {
		super.documentPropertyChanged(doc)
		mainElementIn(doc)?.addEventListener("input", {
			val hc = mainElementIn(doc)?.innerHTML ?: ""
					// webView.engine.executeScript("document.getElementById('editable').innerHTML") as? String ?: ""
			try {
				modifyingHc++
				htmlContent = processor?.parse(hc) ?: hc
			} finally {
				modifyingHc--
			}
		}, false)
	}
	
	override fun templatePartStyle(): String {
		return super.templatePartStyle() + "#mainElement{font-family:'${Styles.FONT_FACE_TEXT}', 'serif'}"
	}
	
	override fun templatePartMainElement(): String {
		return "<div contentEditable='true' id='mainElement' class='$mainElementClassAttr'></div>"
	}
	
}