package ej.editor.views

import ej.editor.utils.XmlParser
import ej.editor.utils.extractInlineStyle
import ej.editor.utils.unescapeXml

/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */
class FlashHtmlProcessor : XmlParser<ReadOnlyFlashTextDocument>() {
	override fun begin(source: String) = object : SimpleStreamProcessor<ReadOnlyFlashTextDocument>() {
		var document = FlashTextDocumentBuilder(FlashSegStyle.segOps, FlashParStyle())
		var paragraph: MutableList<FlashStyledSegment> = ArrayList()
		var curParStyle = FlashParStyle()
		var curSegStyle = FlashSegStyle()
		var eop = true
		var np = 0
		
		private fun addText(text: String) {
			if (eop) {
				startParagraph(curParStyle)
			}
			paragraph.add(FlashStyledSegment(text, curSegStyle))
		}
		
		private fun startParagraph(parStyle: FlashParStyle) {
			endParagraph()
			curParStyle = parStyle
			eop = false
		}
		
		private fun endParagraph() {
			if (paragraph.isNotEmpty()) {
				document.addParagraph(paragraph, curParStyle)
				np++
				paragraph = ArrayList()
				eop = true
			}
		}
		
		override fun doText(text: String) {
			addText(text.unescapeXml())
		}
		
		override fun doOpen(single: Boolean, tag: String, attrs: HashMap<String, String>) {
			val style = extractInlineStyle(attrs["style"])
			when (tag.toLowerCase()) {
				"i" -> {
					curSegStyle = curSegStyle.copy(italic = true)
				}
				"b" -> {
					curSegStyle = curSegStyle.copy(bold = true)
				}
				"u" -> curSegStyle = curSegStyle.copy(underline = true)
				"span" -> {/*curSegStyle = curSegStyle.copy()*/
				}
				"font" -> curSegStyle = curSegStyle.copy(
						color = attrs["color"],
						face = attrs["face"],
						size = attrs["size"]?.toIntOrNull()
				)
				
				"br" -> addText("\n")
				"p" -> {
					if (style["margin-top"]?.startsWith("0") == false
							|| style["margin-bottom"]?.startsWith("0") == false) {
						startParagraph(
								FlashParStyle(
										tag = FlashParStyle.PARTAG_P,
										align = attrs["align"]
								)
						)
					}
				}
				"div" -> {
					startParagraph(
							FlashParStyle(
									tag = FlashParStyle.PARTAG_DIV
							)
					)
				}
			}
			for ((k, v) in style) when (k) {
				"font-weight" -> v.toIntOrNull()?.let { weight ->
					curSegStyle = curSegStyle.copy(bold = weight >= 600)
				}
				"font-style" ->
					curSegStyle = curSegStyle.copy(italic =
					                               when (v) {
						                               "italic" -> true
						                               "normal" -> false
						                               else -> null
					                               }
					)
				"text-decoration" ->
					curSegStyle = curSegStyle.copy(underline =
					                               when (v) {
						                               "underline" -> true
						                               "inherit", "initial" -> null
						                               else -> false
					                               }
					)
			}
		}
		
		override fun doClose(tag: String) {
			when (tag.toLowerCase()) {
				"i" -> curSegStyle = curSegStyle.copy(italic = false)
				"b" -> curSegStyle = curSegStyle.copy(bold = false)
				"u" -> curSegStyle = curSegStyle.copy(underline = false)
				"span" -> {/*curSegStyle = curSegStyle*/
				}
				"font" -> curSegStyle = curSegStyle.clearFont()
				
				"p", "div" -> endParagraph()
			}
		}
		
		override fun end(): ReadOnlyFlashTextDocument {
			endParagraph()
			if (np == 0) document.addParagraph("", curSegStyle)
			return document.build()
		}
	}
}