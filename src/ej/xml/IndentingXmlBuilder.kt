package ej.xml

import ej.utils.peek
import ej.utils.pop
import ej.utils.push

/*
 * Created by aimozg on 24.12.2018.
 * Confidential until published on GitHub
 */
class IndentingXmlBuilder(
		val wrapped: XmlBuilder,
		var indentStyle: IndentStyle = STYLE_INDENTNONE
) : XmlBuilder {
	
	var oneIndent = "\t"
	
	private val stack = arrayListOf(IndentType.INLINE)
	private var indentLevel = 0
	private var wasText = false
	private var wantBreak = false
	private fun flushBreak() {
		if (wantBreak) {
			writeBreak()
		}
	}
	
	private fun writeBreak() {
		wrapped.text("\n" + oneIndent.repeat(indentLevel))
		wantBreak = false
	}
	
	override fun startDocument() {
		wrapped.startDocument()
		val i = indentStyle.elementIndent("?xml")
		stack.push(i)
		if (i.isIndent) indentLevel++
		if (i.isMultiline) wantBreak = true
	}
	
	override fun endDocument() {
		wrapped.endDocument()
		stack.pop()
	}
	
	override fun startElement(tag: String, attrs: Map<String, String>) {
		val outer = stack.peek()
		val inner = indentStyle.elementIndent(tag)
		
		if (outer.isMultiline || inner.isBlock) {
			wantBreak = true
		}
		flushBreak()
		wrapped.startElement(tag, attrs)
		stack.push(inner)
		if (inner.isIndent) indentLevel++
		if (inner.isMultiline) wantBreak = true
		
		wasText = false
	}
	
	override fun endElement() {
		val inner = stack.peek()
		val outer = stack.pop()
		
		if (inner.isIndent) indentLevel--
		if (inner.isMultiline) {
			wantBreak = true
		}
		flushBreak()
		wrapped.endElement()
		if (outer.isMultiline || inner.isBlock) {
			wantBreak = true
		}
		wasText = false
	}
	
	override fun text(data: String) {
		flushBreak()
		wrapped.text(data)
		
		wasText = true
	}
	
	override fun emptyElement(tag: String, attrs: Map<String, String>) {
		val outer = stack.peek()
		val inner = indentStyle.elementIndent(tag)
		
		if (outer.isMultiline || inner.isBlock) {
			wantBreak = true
		}
		flushBreak()
		wrapped.emptyElement(tag, attrs)
		if (outer.isMultiline || inner.isBlock) {
			wantBreak = true
		}
	}
	
	companion object {
		val STYLE_INDENTNONE = object : IndentStyle {
			override fun elementIndent(name: String) = IndentType.INLINE
		}
	}
	
	interface IndentStyle {
		fun elementIndent(name: String): IndentType
	}
	
	class SimpleIndentStyle(val default: IndentType, val mapping: Map<String, IndentType>) : IndentStyle {
		override fun elementIndent(name: String) = mapping[name] ?: default
	}
	
	enum class IndentType(val isBlock: Boolean, val isMultiline: Boolean, val isIndent: Boolean) {
		/**
		 *     content <a>txt1<b/>txt2</a> content
		 *     ->
		 *     content <a>txt1<b/>txt2</a> content
		 */
		INLINE(false, false, false),
		/**
		 *     content <a>txt1<b/>txt2</a> content
		 *     ->
		 *     content
		 *     <a>txt1<b/>txt2</a>
		 *      content
		 */
		BLOCK(true, false, false),
		/**
		 *     content <a>txt1<b/>txt2</a> content
		 *     ->
		 *     content
		 *     <a>
		 *     txt1
		 *     <b/>
		 *     txt2
		 *     </a>
		 *      content
		 */
		MULTILINE(true, true, false),
		/**
		 *     content <a>txt1<b/>txt2</a> content
		 *     ->
		 *     content
		 *     <a>
		 *         txt1
		 *         <b/>
		 *         txt2
		 *     </a>
		 *      content
		 */
		INDENTED(true, true, true)
	}
}