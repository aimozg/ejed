package ej.editor.utils

import ej.utils.crop

/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */
abstract class XmlParser<OUT> : AbstractParser<OUT>() {
	protected interface StreamProcessor<OUT> {
		fun doText(text: String) //
		fun doBegin(tag: String) // <tag
		fun doAttr(name: String, value: String) // name="value"
		fun doOpen(single: Boolean) // > or />
		fun doClose(tag: String) // </tag>
		fun end(): OUT
	}
	
	protected abstract class SimpleStreamProcessor<OUT> : StreamProcessor<OUT> {
		private var tag: String = ""
		private var attrs: HashMap<String, String> = HashMap()
		final override fun doBegin(tag: String) {
			this.tag = tag
			this.attrs = HashMap()
		}
		
		final override fun doAttr(name: String, value: String) {
			this.attrs[name] = value
		}
		
		final override fun doOpen(single: Boolean) {
			doOpen(single, tag, attrs)
		}
		
		abstract fun doOpen(single: Boolean, tag: String, attrs: HashMap<String, String>)
	}
	
	protected abstract fun begin(source: String): StreamProcessor<OUT>
	
	override fun Context.doParse(): OUT {
		val state = begin(source)
		while (isNotEmpty()) {
			val text = eatenUntil("<")
			if (text?.isNotEmpty() == true) state.doText(text)
			if (eat(LA_BEGIN)) {
				// <element
				val tag = match.groupValues[1]
				state.doBegin(tag)
				val single: Boolean
				while (true) {
					eatWs()
					if (eat(LA_ATTR) || eat(LA_FLAG)) {
						val attrname = match.groupValues[1]
						val attrvalue = match.groupValues[2]
						state.doAttr(attrname, attrvalue)
					} else if (eat(LA_OPEN_OR_SINGLE)) {
						single = eaten == "/>"
						break
					} else {
						println("[WARN] Malformed XML near ${str.crop(20)}")
						eat(1)
					}
				}
				state.doOpen(single)
			} else if (eat(LA_END)) {
				// </element>
				val tag = match.groupValues[1]
				state.doClose(tag)
			} else if (str.indexOf('<') == -1) {
				break
			} else if (isNotEmpty()) {
				state.doText(eaten(1))
			} else break
		}
		if (str.isNotEmpty()) state.doText(str)
		return state.end()
	}
	
	companion object {
		private val LA_BEGIN = Regex("""^<([^"</>&=\s]++)""") // 1 = tag name
		private val LA_ATTR = Regex("""^([^"</>&=\s]++)="?([^"<>]++)"?""") // 1 = name, 2 = value
		private val LA_FLAG = Regex("""^([^"</>&=\s]++)()(?!=)""") // 1 = name, 2 = empty
		private val LA_OPEN_OR_SINGLE = Regex("""^/?>""")
		private val LA_END = Regex("""^</([^"</>&=\s]++)>""") // 1 = tag name
	}
}