package ej.editor.utils

import java.io.OutputStream

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */
class SplittingOutputStream(private vararg val streams: OutputStream) : OutputStream() {
	override fun write(b: Int) {
		for (s in streams) s.write(b)
	}
	
	override fun write(b: ByteArray) {
		for (s in streams) s.write(b)
	}
	
	override fun write(b: ByteArray?, off: Int, len: Int) {
		for (s in streams) s.write(b, off, len)
	}
	
	override fun flush() {
		for (s in streams) s.flush()
	}
	
	override fun close() {
		for (s in streams) s.close()
	}
}