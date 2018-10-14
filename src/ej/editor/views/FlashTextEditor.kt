package ej.editor.views

import ej.editor.utils.WritableExpression
import ej.utils.crop
import javafx.beans.property.Property
import javafx.geometry.Orientation
import javafx.scene.input.Clipboard
import javafx.scene.input.ClipboardContent
import javafx.scene.input.ScrollEvent
import javafx.scene.text.TextFlow
import org.fxmisc.flowless.VirtualFlow
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.model.Codec
import org.fxmisc.richtext.model.StyleSpans
import tornadofx.*
import java.io.IOException
import java.util.function.BiConsumer


/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */
class FlashTextEditor(document: EditableFlashTextDocument) :
		StyledTextArea<FlashParStyle, FlashSegStyle>(
				FlashParStyle(),
				BiConsumer<TextFlow, FlashParStyle> { t, u ->
					u.applyTo(t)
				},
				FlashSegStyle(),
				BiConsumer<TextExt, FlashSegStyle> { t, u ->
					u.applyTo(t)
				},
				document
		) {
	constructor(text: String) : this(FlashHtmlProcessor().parse(text).editableCopy())
	constructor(textProperty: Property<String>) : this(
			EditableFlashTextDocument(FlashParStyle(), FlashSegStyle())
	) {
		textProperty.unbind()
		editableTextProperty.unbind()
		editableTextProperty.bindBidirectional(textProperty)
	}
	
	val editableTextProperty = object : WritableExpression<String>() {
		override fun doGet(): String {
			return document.toFlashHtml()
		}
		
		override fun doSet(value: String) {
			println("doSet " + value.crop(80))
			loadText(value)
		}
		
	}
	var editableText by editableTextProperty
	var isAutoStretch = false
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	fun disableScrollEvents() {
		addEventFilter(ScrollEvent.SCROLL) {
			it.consume()
			parent?.fireEvent(it.copyFor(it.source, parent))
		}
	}
	
	override fun computePrefHeight(width: Double): Double {
		if (isAutoStretch) {
			val ih = insets.top + insets.bottom
			val c = children.firstOrNull() as? VirtualFlow<*, *>
			if (c != null && paragraphs.size > 0) {
				val cw = (c.getWidth() - c.getInsets().left - c.getInsets().right).takeIf { it > 0 }
						?: (width - insets.left - insets.right)
				return ih + (0 until paragraphs.size).sumByDouble { i ->
					c.getCell(i).node.prefHeight(cw)
				}
			}
			val d = totalHeightEstimateProperty().value
			if (d != null) return ih + d
			runLater { requestLayout() }
		}
		return super.computePrefHeight(width)
	}
	
	fun loadText(text: String) {
		this.content.replace(0, this.content.length, FlashHtmlProcessor().parse(text))
	}
	
	init {
		richChanges().subscribe {
			editableTextProperty.markInvalid()
		}
		setStyleCodecs(FlashParStyle.CODEC, Codec.styledTextCodec(FlashSegStyle.CODEC))
		FlashTextEditorBehaviour(this)
	}
	
	override fun copy() {
		val selection = selection
		if (selection.length > 0) {
			val content = ClipboardContent()
			
			val doc = subDocument(selection.start, selection.end)
			content.putString(doc.text)
			content.putHtml(doc.toFlashHtml())
			
			try {
				content[FLASH_TEXT_DOCUMENT_FORMAT] = FLASH_TEXT_DOCUMENT_CODEC.encode(doc)
			} catch (e: IOException) {
				System.err.println("Codec error: Exception in encoding FlashTextDocument:")
				e.printStackTrace()
			}
			
			Clipboard.getSystemClipboard().setContent(content)
		}
	}
	
	override fun paste() {
		val clipboard = Clipboard.getSystemClipboard()
		
		val doc =
				(clipboard.getContent(FLASH_TEXT_DOCUMENT_FORMAT) as? ByteArray?)?.let { bytes ->
					try {
						FLASH_TEXT_DOCUMENT_CODEC.decode(bytes)
					} catch (e: IOException) {
						System.err.println("Codec error: Failed to decode FlashTextDocument:")
						e.printStackTrace()
						null
					}
				}
						?: clipboard.html?.let { html ->
							try {
								FlashHtmlProcessor().parse(html)
							} catch (e: Exception) {
								System.err.println("Failed to parse clipboard content")
								null
							}
						}
		if (doc != null) {
			replaceSelection(doc)
		} else {
			val s = clipboard.string
			if (s != null) replaceSelection(s)
		}
	}
	
	private fun updateStyleInSelection(mixinGetter: (StyleSpans<FlashSegStyle>) -> FlashSegStyle) {
		val selection = selection
		if (selection.length != 0) {
			val styles = getStyleSpans(selection)
			val mixin = mixinGetter(styles)
			val newStyles = styles.mapStyles { style -> style.updateWith(mixin) }
			setStyleSpans(selection.start, newStyles)
		}
	}
	
	private fun updateStyleInSelection(mixin: FlashSegStyle) {
		val selection = selection
		if (selection.length != 0) {
			val styles = getStyleSpans(selection)
			val newStyles = styles.mapStyles { style -> style.updateWith(mixin) }
			setStyleSpans(selection.start, newStyles)
		}
	}
	
	fun boldSelection() {
		updateStyleInSelection {
			FlashSegStyle(
					bold = !(it.styleStream().findFirst().orElse(null)?.bold ?: false),
					italic = null,
					underline = null
			)
		}
	}
	
	fun italizeSelection() {
		updateStyleInSelection {
			FlashSegStyle(
					bold = null,
					italic = !(it.styleStream().findFirst().orElse(null)?.italic ?: false),
					underline = null
			)
		}
	}
	
	fun underlineSelection() {
		updateStyleInSelection {
			FlashSegStyle(
					bold = null,
					italic = null,
					underline = !(it.styleStream().findFirst().orElse(null)?.underline ?: false)
			)
		}
	}
}