package ej.editor.views

import ej.editor.utils.XmlTextProcessor

class FlashTextProcessor : XmlTextProcessor() {
	override fun testBegin(tag: String) = when(tag) {
//		"div" -> renameTag("br")
		"p" -> renameTag("br")
		in FlashPermittedElements -> take()
		else -> skip()
	}
	var skippedSpans:Int = 0
	
	override fun testOpen(tag: String, attrs: StringBuilder,single:Boolean) =
			if (tag == "span" && attrs.isBlank()) {
				skippedSpans++
				skip()
			} else take()
	
	override fun testEnd(tag: String) = when(tag) {
		"span" -> if (skippedSpans > 0) {
			skippedSpans--
			skip()
		} else take()
		"div" -> skip()
		in FlashPermittedElements -> take()
		else -> skip()
	}
	
	override fun testAttr(tag: String, name: String, value: String) =
			when(tag) {
				in FlashPermittedElements ->
					if (name in FlashPermittedElements.getOrDefault(name,emptySet())) take()
					else skip()
				else -> skip()
			}
	companion object {
		private val FlashPermittedElements:Map<String,Set<String>> = mapOf(
				"a" to setOf("target","href"),
				"b" to emptySet(),
				"br" to emptySet(),
				"font" to setOf("color","face","size"),
				"i" to emptySet(),
				"img" to setOf("src","width","height","align","hspace","vspace","id","checkPolicyFile"),
				"li" to emptySet(),
//				"p" to setOf("class","align"),
				"span" to setOf("class"),
				"textformat" to setOf("blockindent", "indent", "leading", "leftmargin", "rightmargin", "tabstops"),
				"u" to emptySet()
		)
	}
}