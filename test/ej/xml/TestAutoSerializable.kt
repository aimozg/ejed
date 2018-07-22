@file:Suppress("unused")

package ej.xml

import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter
import kotlin.reflect.KClass
import kotlin.test.assertFails

/*
 * Created by aimozg on 22.07.2018.
 * Confidential until published on GitHub
 */
class TestAutoSerializable {
	
	@Before
	fun clearCache() {
		KnownSzInfos.clear()
	}
	
	fun XmlExplorerController.assertNoElements() {
		forEachElement { tag, _ ->
			assertEquals("(no elements)", "<$tag>")
		}
	}
	
	inline fun <T : XmlAutoSerializable, R : Any>
			assertXml(src: T,
			          reverse: Boolean = true,
			          expected: XmlExplorerController.() -> R): R {
		val output = StringWriter()
		val builder = XmlBuilder(output)
		
		@Suppress("UNCHECKED_CAST")
		val szInfo = getSerializationInfo(src::class as KClass<T>)
		szInfo.serializeDocument(src, builder)
		val actual = output.buffer.toString()
		val r = XmlExplorer(StringReader(actual)).run(expected)
		
		if (reverse) {
			val src2 = szInfo.deserializeDocument(XmlExplorer(StringReader(actual)))
			val output2 = StringWriter()
			val builder2 = XmlBuilder(output2)
			szInfo.serializeDocument(src2, builder2)
			assertEquals(output2.buffer.toString(), actual)
		}
		
		return r
	}
	
	fun <T : XmlAutoSerializable, R : Any>
			assertXml(src: T,
			          rootElement: String,
			          reverse: Boolean = true,
			          expected: XmlExplorerController.(rootAttrs: Map<String, String>) -> R): R {
		return assertXml(src, reverse) {
			exploreDocument(rootElement, expected)
		}
	}
	
	fun assertAttrs(actual: Map<String, String>, vararg expected: Pair<String, String>) {
		assertEquals(mapOf(*expected), actual)
	}
	
	@Test
	fun testEmpty() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			assertNoElements()
		}
	}
	
	@Test
	fun testStringAttributes() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Attribute
			var s: String = "123"
			@Attribute("name2")
			var s2: String = "456"
			@Attribute
			var s3: String? = null
			@Attribute
			var s4: String? = "789"
		}
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs,
			            "s" to "123",
			            "name2" to "456",
			            "s4" to "789")
			assertNoElements()
		}
	}
	
	@Test
	fun testIntAttributes() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Attribute
			var s: Int = 123
			@Attribute
			var s2: Int? = null
			@Attribute
			var s3: Int? = 456
		}
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs,
			            "s" to "123",
			            "s3" to "456")
			assertNoElements()
		}
	}
	
	@Test
	fun testBoolAttributes() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Attribute
			var s: Boolean = true
			@Attribute
			var s2: Boolean = false
			@Attribute
			var s3: Boolean? = null
			@Attribute
			var s4: Boolean? = true
			@Attribute
			var s5: Boolean? = false
		}
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs,
			            "s" to "true",
			            "s4" to "true",
			            "s5" to "false")
			assertNoElements()
		}
	}
	
	@Test
	fun testUnknownAttrType() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Attribute
			var s: Any = System.getSecurityManager()
		}
		assertFails {
			getSerializationInfo(Mock::class)
		}
		@RootElement("mock")
		class Mock2 : XmlAutoSerializable {
			@Attribute
			var s: Mock = Mock()
		}
		assertFails {
			getSerializationInfo(Mock2::class)
		}
	}
	
	@Test
	fun textUnknownElemType() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Element
			var s: Any = System.getSecurityManager()
		}
		assertFails {
			getSerializationInfo(Mock::class)
		}
	}
	
	@Test
	fun textUnknownTextType() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@TextBody
			var s: Any = System.getSecurityManager()
		}
		assertFails {
			getSerializationInfo(Mock::class)
		}
	}
	
	@Test
	fun testSimpleElements() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Element
			var s1: String = "123"
			@Element
			var s2: String? = null
			@Element
			var s3: String? = "456"
			@Element
			var s4: Int = 789
			@Element
			var s5: Int? = null
			@Element
			var s6: Int? = 1011
			@Element
			var s7: Boolean = true
			@Element
			var s8: Boolean = false
			@Element
			var s9: Boolean? = true
			@Element
			var s10: Boolean? = false
			@Element
			var s11: Boolean? = null
			@Element("name")
			var s12: String = "1213"
		}
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			forEachElement { tag, attrs ->
				assertAttrs(attrs)
				when (tag) {
					"s1" -> assertEquals("123", text())
					"s3" -> assertEquals("456", text())
					"s4" -> assertEquals("789", text())
					"s6" -> assertEquals("1011", text())
					"s7" -> assertEquals("true", text())
					"s9" -> assertEquals("true", text())
					"s10" -> assertEquals("false", text())
					"name" -> assertEquals("1213", text())
					else -> fail("Unexpected $tag")
				}
			}
		}
	}
	
	@Test
	fun testTextBody() {
		@RootElement("mock")
		class Mock1 : XmlAutoSerializable {
			@TextBody
			var value: String = "123"
			@Attribute
			var another: String = "456"
		}
		assertXml(Mock1(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs, "another" to "456")
			assertEquals("123", text())
		}
		@RootElement("mock")
		class Mock2 : XmlAutoSerializable {
			@TextBody
			var value: String? = "123"
		}
		assertXml(Mock2(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			assertEquals("123", text())
		}
		@RootElement("mock")
		class Mock3 : XmlAutoSerializable {
			@TextBody
			var value: String? = null
		}
		assertXml(Mock3(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			assertEquals("", text())
		}
	}
	
	@Test
	fun testSingleElement() {
		class Mock1(@Attribute var me: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Mock2 : XmlAutoSerializable {
			@Element
			var e1 = Mock1("123")
			@Element("named")
			var e2 = Mock1("456")
			@Element
			var e3: Mock1? = null
			@Element
			var e4: Mock1? = Mock1("789")
		}
		assertXml(Mock2(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			forEachElement { tag, attrs ->
				when (tag) {
					"e1" -> assertAttrs(attrs, "me" to "123")
					"named" -> assertAttrs(attrs, "me" to "456")
					"e4" -> assertAttrs(attrs, "me" to "789")
					else -> fail("Unexpected mock.$tag")
				}
			}
		}
	}
	
	@Test
	fun testLists() {
		class Mock1(@TextBody var init: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Mock2(vararg init: String) : XmlAutoSerializable {
			@Elements("thing")
			val things: MutableList<Mock1> = init.map { Mock1(it) }.toMutableList()
		}
		assertXml(Mock2("a", "b"), "mock") { rootAttrs ->
			assertAttrs(rootAttrs)
			val things = collectElements { tag, attrs ->
				when (tag) {
					"thing" -> {
						assertAttrs(attrs)
						text()
					}
					else -> error("Unexpected mock.$tag")
				}
			}
			assertEquals(listOf("a", "b"), things)
		}
	}
	
	@Test
	fun testInheritance() {
		open class Mock1
		open class Mock2 : Mock1(), XmlAutoSerializable {
			@Element
			var a = "123"
			@Attribute
			var b = "456"
		}
		
		@RootElement("mock")
		open class Mock3 : Mock2() {
			@Element
			var c = "789"
			@Attribute
			var d = "1011"
		}
		assertXml(Mock3(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs, "b" to "456", "d" to "1011")
			assertEquals(
					listOf("a" to "123", "c" to "789"),
					collectElements { tag, attrs ->
						assertAttrs(attrs)
						tag to text()
					}
			)
		}
	}
}