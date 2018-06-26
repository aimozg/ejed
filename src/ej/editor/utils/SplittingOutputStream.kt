package ej.editor.utils

import java.io.OutputStream

/*
 * Created by aimozg on 26.06.2018.
 * Confidential until published on GitHub
 */
class SplittingOutputStream(private val first:OutputStream, private val second:OutputStream): OutputStream() {
	override fun write(b: Int) {
		first.write(b)
		second.write(b)
	}
	
	override fun write(b: ByteArray) {
		first.write(b)
		second.write(b)
	}
	
	override fun write(b: ByteArray?, off: Int, len: Int) {
		first.write(b, off, len)
		second.write(b, off, len)
	}
	
	override fun flush() {
		first.flush()
		second.flush()
	}
	
	override fun close() {
		first.close()
		second.close()
	}
}