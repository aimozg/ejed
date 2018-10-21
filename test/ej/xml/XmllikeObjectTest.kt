package ej.xml

import ej.utils.iAmEitherLeft
import ej.utils.iAmEitherRight
import junit.framework.TestCase

/*
 * Created by aimozg on 21.10.2018.
 * Confidential until published on GitHub
 */
class XmllikeObjectTest : TestCase() {
	lateinit var a: XmllikeObject
	override fun setUp() {
		a = XmllikeObject("a", mapOf("b" to "c", "d" to "e"),
		                  "f".iAmEitherLeft(),
		                  XmllikeObject("g").iAmEitherRight())
	}
	
	fun testEquals() {
		val b = XmllikeObject("a", mapOf("b" to "c", "d" to "e"),
		                      "f".iAmEitherLeft(),
		                      XmllikeObject("g").iAmEitherRight())
		val name = XmllikeObject("A", mapOf("b" to "c", "d" to "e"),
		                         "f".iAmEitherLeft(),
		                         XmllikeObject("g").iAmEitherRight())
		val attrs = XmllikeObject("a", mapOf("b" to "c"),
		                          "f".iAmEitherLeft(),
		                          XmllikeObject("g").iAmEitherRight())
		val content = XmllikeObject("a", mapOf("b" to "c", "d" to "e"),
		                            XmllikeObject("g").iAmEitherRight())
		assertTrue(a == b)
		assertFalse(a == name)
		assertFalse(a == attrs)
		assertFalse(a == content)
	}
	
	fun testSerialization() {
		assertEquals(a, XmllikeObject.fromBytes(a.toBytes()))
		val b = XmllikeObject("h", mapOf("i" to "j"), a.iAmEitherRight())
		assertEquals(b, XmllikeObject.fromBytes(b.toBytes()))
	}
}