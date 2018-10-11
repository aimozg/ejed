package ej.editor.views

import ej.editor.utils.WritableExpression
import javafx.beans.property.Property
import javafx.geometry.Orientation
import javafx.scene.text.TextFlow
import org.fxmisc.flowless.VirtualFlow
import org.fxmisc.richtext.StyledTextArea
import org.fxmisc.richtext.TextExt
import tornadofx.*
import java.util.function.BiConsumer

/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */
open class FlashTextEditor(document: EditableFlashTextDocument) :
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
		editableTextProperty.bindBidirectional(textProperty)
	}
	
	val editableTextProperty = object : WritableExpression<String>() {
		override fun doGet(): String {
			return document.toFlashHtml()
		}
		
		override fun doSet(value: String) {
			loadText(value)
		}
		
	}
	var editableText by editableTextProperty
	var isAutoStretch = false
	override fun getContentBias(): Orientation {
		return Orientation.HORIZONTAL
	}
	
	override fun computePrefHeight(width: Double): Double {
		if (isAutoStretch) {
			val ih = insets.top + insets.bottom
			val c = children.firstOrNull() as? VirtualFlow<*, *>
			if (c != null && paragraphs.size > 0) {
				return ih + (0 until paragraphs.size).sumByDouble { i ->
					c.getCell(i).node.prefHeight(width)
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
	}
}