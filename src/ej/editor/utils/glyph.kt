package ej.editor.utils

import javafx.beans.value.ObservableValue
import org.controlsfx.glyphfont.FontAwesome
import org.controlsfx.glyphfont.Glyph
import org.controlsfx.glyphfont.GlyphFontRegistry

/*
 * Created by aimozg on 25.07.2018.
 * Confidential until published on GitHub
 */

val fontAwesome = GlyphFontRegistry.font("FontAwesome")

fun boundFaGlyph(binding:ObservableValue<out String>):Glyph = fontAwesome.create(" ").apply {
	textProperty().bind(binding)
}
fun FontAwesome.Glyph.node(): Glyph = fontAwesome.create(this)
