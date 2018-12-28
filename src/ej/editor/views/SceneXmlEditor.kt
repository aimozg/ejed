package ej.editor.views

import ej.utils.length
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.GenericStyledArea
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.model.*
import tornadofx.*
import java.util.function.BiConsumer
import java.util.function.Function


/*
 * Created by aimozg on 24.12.2018.
 *
 * Lots of stuff from
 * https://github.com/FXMisc/RichTextFX/blob/master/richtextfx-demos/src/main/java/org/fxmisc/richtext/demo/XMLEditorDemo.java
 *
 * Also see inheritance of StyleClassedTextArea < StyledTextArea < GenericStyledTextArea - many things just copied
 */
class SceneXmlEditor() :
		GenericStyledArea<Collection<String>, String, Collection<String>>(
				initialParagraphStyle,
				applyParagraphStyle,
				initialTextStyle,
				initialDocument,
				segmentOps,
				preserveStyle,
				nodeFactory
		) {
	init {
		addClass("scene-xml-editor")
		// don't apply preceding style to typed text
		useInitialStyleForInsertion = true
		isWrapText = true
		textProperty().addListener { _, _, newText ->
			setStyleSpans(0, computeHighlighting(newText))
		}
	}
	
	constructor(text: String) : this() {
		appendText(text)
		undoManager.forgetHistory()
		undoManager.mark()
		selectRange(0, 0)
	}
	
	companion object {
		private val XML_TAG = Regex(
				"""((</?\h*)(\w+)((?:[^</>]|/[^>])*)(\h*/?>))|(<!--(?:[^-]|-(?!->))+-->)|(\t++)"""
				// 12       3    4                  5         6                          7
				//  start   name attribute-body     end       comment                    tab
		)
		private val ATTRIBUTES = Regex(
				"""(\w+\h*)(=)(\h*"[^"]+")"""
				// 1       2  3
		)
		
		
		fun computeHighlighting(text: String): StyleSpans<Collection<String>> {
			val spansBuilder = StyleSpansBuilder<Collection<String>>()
			var pos = 0
			var tdepth = 0
			for (mr in XML_TAG.findAll(text)) {
				spansBuilder.add(if (tdepth > 0) listOf("output") else listOf("chars"), mr.range.first - pos)
				when {
					mr.groups[1] != null -> {
						// tag
						val open = mr.groups[2]!!
						val name = mr.groups[3]!!
						val attrPart = mr.groups[4]!!
						val close = mr.groups[5]!!
						val isSingle = close.value.endsWith("/>")
						val isClose = open.value.startsWith("</")
						val isOpen = !isSingle && !isClose
						if (name.value.toLowerCase() == "t") {
							if (isOpen) tdepth++
							if (isClose) tdepth--
						}
						spansBuilder.add(listOf("tagc"), open.length)
						spansBuilder.add(listOf("tagn"), name.length)
						if (!attrPart.range.isEmpty()) {
							var mpos = 0
							for (amr in ATTRIBUTES.findAll(attrPart.value)) {
								spansBuilder.add(emptyList(), amr.range.first - mpos)
								// 1=name, 2='=', 3=value
								spansBuilder.add(listOf("tagan"), amr.groups[1]!!.length)
								spansBuilder.add(listOf("tagaq"), amr.groups[2]!!.length)
								spansBuilder.add(listOf("tagav"), amr.groups[3]!!.length)
								mpos = amr.range.last + 1
							}
							spansBuilder.add(emptyList(), attrPart.length - mpos)
						}
						spansBuilder.add(listOf("tagc"), close.length)
					}
					mr.groups[6] != null -> {
						// comment
						spansBuilder.add(listOf("comment"), mr.length)
					}
					mr.groups[7] != null -> {
						// tab
						spansBuilder.add(listOf("tab"), mr.length)
					}
					else -> {
						spansBuilder.add(emptyList(), mr.length)
					}
				}
				pos = mr.range.last + 1
			}
			spansBuilder.add(listOf("chars"), text.length - pos)
			return spansBuilder.create()
		}
		
		private val initialParagraphStyle: Collection<String> = emptyList()
		private val applyParagraphStyle: BiConsumer<TextFlow, Collection<String>> = BiConsumer { paragraph, styleClasses ->
			paragraph.styleClass.addAll(styleClasses)
		}
		private val initialTextStyle: Collection<String> = emptyList()
		private val initialDocument: EditableStyledDocument<Collection<String>, String, Collection<String>> = SimpleEditableStyledDocument(
				emptyList(),
				emptyList())
		private val segmentOps: TextOps<String, Collection<String>> = SegmentOps.styledTextOps()
		private const val preserveStyle = true
		private val nodeFactory: Function<StyledSegment<String, Collection<String>>, Node> = Function { seg ->
			TextExt(seg.segment).apply {
				textOrigin = VPos.TOP
				styleClass.add("text")
				styleClass.addAll(seg.style)
			}
		}
	}
}