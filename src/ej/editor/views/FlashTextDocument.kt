package ej.editor.views

/*
 * Created by aimozg on 11.10.2018.
 * Confidential until published on GitHub
 */

import ej.utils.plusAssign
import javafx.scene.text.FontPosture
import javafx.scene.text.FontWeight
import javafx.scene.text.TextAlignment
import javafx.scene.text.TextFlow
import org.fxmisc.richtext.TextExt
import org.fxmisc.richtext.model.*
import tornadofx.*
import java.util.*

data class FlashParStyle(
		val tag: String? = null,
		val align: String? = null
) {
	fun applyTo(t: TextFlow) {
		t.style {
			when (align) {
				ALIGH_LEFT -> textAlignment = TextAlignment.LEFT
				ALIGH_CENTER -> textAlignment = TextAlignment.CENTER
				ALIGH_RIGHT -> textAlignment = TextAlignment.RIGHT
				ALIGH_JUSTIFY -> textAlignment = TextAlignment.JUSTIFY
			}
			when (tag) {
				PARTAG_P -> padding.top + 0.5.em
			}
		}
	}
	
	fun toFlashHtml(styledSegments: List<StyledSegment<String, FlashSegStyle>>): CharSequence {
		val before: String
		val after: String
		if (tag != null) {
			before = "<$tag" + (if (align != null) " align=\"$align\"" else "") + ">"
			after = "</$tag>"
		} else {
			before = ""
			after = ""
		}
		return styledSegments.joinToString("", before, after) { it.style.toFlashHtml(it.segment) }
	}
	
	companion object {
		const val PARTAG_DIV = "div"
		const val PARTAG_P = "p"
		const val ALIGH_LEFT = "left"
		const val ALIGH_CENTER = "center"
		const val ALIGH_RIGHT = "right"
		const val ALIGH_JUSTIFY = "justify"
	}
}


data class FlashSegStyle(
		val bold: Boolean = false,
		val italic: Boolean = false,
		val underline: Boolean = false,
		val color: String? = null,
		val size: Int? = null,
		val face: String? = null
) {
	
	fun clearFont() =
			FlashSegStyle(
					bold = bold,
					italic = italic,
					underline = underline
			)
	
	fun applyTo(t: TextExt) {
		val s = this
		t.style {
			if (s.bold) fontWeight = FontWeight.BOLD
			if (s.italic) fontStyle = FontPosture.ITALIC
			if (s.underline) underline = true
			if (s.color != null) textFill = c(s.color)
			if (s.size != null) fontSize = s.size.pt
			if (s.face != null) fontFamily = s.face
		}
	}
	
	fun mergeWith(u: FlashSegStyle): Optional<FlashSegStyle> {
		if (
				different(this.bold, u.bold) ||
				different(this.italic, u.italic) ||
				different(this.underline, u.underline) ||
				different(this.color, u.color) ||
				different(this.size, u.size) ||
				different(this.face, u.face)
		) return Optional.empty()
		return Optional.of(FlashSegStyle(
				this.bold,
				this.italic,
				this.underline,
				this.color ?: u.color,
				this.size ?: u.size,
				this.face ?: u.face
		))
	}
	
	fun toFlashHtml(segment: String): CharSequence {
		val before = StringBuilder()
		val after = StringBuilder()
		
		if (bold) {
			before += "<b>"
			after += "</b>"
		}
		if (italic) {
			before += "<i>"
			after += "</i>"
		}
		if (underline) {
			before += "<u>"
			after += "</u>"
		}
		if (color != null || size != null || face != null) {
			before += "<font"
			if (color != null) before += " color=\"$color\""
			if (size != null) before += " size=\"$size\""
			if (face != null) before += " face=\"$face\""
			before += ">"
			after += "</font>"
		}
		before += segment
		before += after
		return before
	}
	
	companion object {
		val segOps = SegmentOps.styledTextOps<FlashSegStyle> { t, u ->
			t.mergeWith(u)
		}!!
	}
}

private fun <T> different(a: T?, b: T?): Boolean = a != null && b != null && a != b

typealias FlashTextDocument = StyledDocument<FlashParStyle, String, FlashSegStyle>
typealias ReadOnlyFlashTextDocument = ReadOnlyStyledDocument<FlashParStyle, String, FlashSegStyle>
typealias EditableFlashTextDocument = SimpleEditableStyledDocument<FlashParStyle, FlashSegStyle>
typealias FlashTextDocumentBuilder = ReadOnlyStyledDocumentBuilder<FlashParStyle, String, FlashSegStyle>
typealias FlashStyledSegment = StyledSegment<String, FlashSegStyle>

fun ReadOnlyFlashTextDocument.editableCopy() = EditableFlashTextDocument(this)
fun FlashTextDocument.toFlashHtml() = paragraphs.joinToString("") {
	it.paragraphStyle.toFlashHtml(it.styledSegments)
}