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
import java.io.DataInputStream
import java.io.DataOutputStream
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
				PARTAG_P -> padding = box(0.5.em, 0.px, 0.px, 0.px)
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
		
		val CODEC = object : Codec<FlashParStyle> {
			override fun getName() = "FlashParStyle"
			
			override fun encode(os: DataOutputStream, t: FlashParStyle) {
				val flags = (if (t.tag != null) 1 else 0) or
						(if (t.align != null) 2 else 0)
				os.writeByte(flags)
				if (t.tag != null) os.writeUTF(t.tag)
				if (t.align != null) os.writeUTF(t.align)
			}
			
			override fun decode(`is`: DataInputStream): FlashParStyle {
				val flags = `is`.readByte().toInt()
				val tag = if (flags.and(1) != 0) `is`.readUTF() else null
				val align = if (flags.and(2) != 0) `is`.readUTF() else null
				return FlashParStyle(tag, align)
			}
		}
	}
}


data class FlashSegStyle(
		val bold: Boolean? = false,
		val italic: Boolean? = false,
		val underline: Boolean? = false,
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
			if (s.bold == true) fontWeight = FontWeight.BOLD
			if (s.italic == true) fontStyle = FontPosture.ITALIC
			if (s.underline == true) underline = true
			if (s.color != null) textFill = c(s.color)
			if (s.size != null) fontSize = s.size.pt
			if (s.face != null) fontFamily = s.face
		}
	}
	
	fun updateWith(u: FlashSegStyle): FlashSegStyle {
		return FlashSegStyle(
				u.bold ?: this.bold,
				u.italic ?: this.italic,
				u.underline ?: this.underline,
				u.color ?: this.color,
				u.size ?: this.size,
				u.face ?: this.face
		)
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
				this.bold ?: u.bold,
				this.italic ?: u.italic,
				this.underline ?: u.underline,
				this.color ?: u.color,
				this.size ?: u.size,
				this.face ?: u.face
		))
	}
	
	fun toFlashHtml(segment: String): CharSequence {
		val before = StringBuilder()
		val after = StringBuilder()
		
		if (bold == true) {
			before += "<b>"
			after += "</b>"
		}
		if (italic == true) {
			before += "<i>"
			after += "</i>"
		}
		if (underline == true) {
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
		
		val CODEC = object : Codec<FlashSegStyle> {
			override fun getName() = "FlashSegStyle"
			
			override fun encode(os: DataOutputStream, t: FlashSegStyle) {
				val flags = (if (t.bold == true) 1 else 0) or
						(if (t.italic == true) 2 else 0) or
						(if (t.underline == true) 4 else 0) or
						(if (t.color != null) 8 else 0) or
						(if (t.size != null) 16 else 0) or
						(if (t.face != null) 32 else 0)
				os.writeByte(flags)
				if (t.color != null) os.writeUTF(t.color)
				if (t.size != null) os.writeInt(t.size)
				if (t.face != null) os.writeUTF(t.face)
			}
			
			override fun decode(`is`: DataInputStream): FlashSegStyle {
				val flags = `is`.readByte().toInt()
				val bold = flags.and(1) != 0
				val italic = flags.and(2) != 0
				val underline = flags.and(4) != 0
				val color = if (flags.and(8) != 0) `is`.readUTF() else null
				val size = if (flags.and(16) != 0) `is`.readInt() else null
				val face = if (flags.and(32) != 0) `is`.readUTF() else null
				return FlashSegStyle(bold, italic, underline, color, size, face)
			}
		}
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