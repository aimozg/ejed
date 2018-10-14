@file:Suppress("unused")

package ej.xml

import ej.utils.iAmEitherLeft
import ej.utils.iAmEitherRight
import org.funktionale.either.Either
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import java.io.StringReader
import java.io.StringWriter
import kotlin.test.assertFails
import kotlin.test.assertNotNull

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
	
	inline fun <reified T : XmlAutoSerializable>
			assertDeserialization(
			expected: T,
			actualString: String) {
		assertDeserialization(expected, actualString, getSerializationInfo())
	}
	
	fun <T : XmlAutoSerializable>
			assertDeserialization(
			expected: T,
			actualString: String,
			szInfo: XmlSerializationInfo<T>) {
		val actual = szInfo.deserializeDocument(XmlExplorer(StringReader(actualString)))
		assertEquals(expected, actual)
	}
	
	inline fun <reified T : XmlAutoSerializable, R : Any>
			assertXml(src: T,
			          reverse: Boolean = true,
			          expected: XmlExplorerController.() -> R): R {
		val output = StringWriter()
		val builder = XmlBuilder(output)
		
		@Suppress("UNCHECKED_CAST")
		val szInfo = getSerializationInfo<T>()
		szInfo.serializeDocument(src, builder)
		val actual = output.buffer.toString()
		val r = try {
			XmlExplorer(StringReader(actual)).run(expected)
		} catch (e: Exception) {
			throw AssertionError("In $actual:", e)
		}
		
		if (reverse) {
			val src2 = szInfo.deserializeDocument(XmlExplorer(StringReader(actual)))
			val output2 = StringWriter()
			val builder2 = XmlBuilder(output2)
			szInfo.serializeDocument(src2, builder2)
			assertEquals(output2.buffer.toString(), actual)
		}
		
		return r
	}
	
	inline fun <reified T : XmlAutoSerializable, R : Any>
			assertXml(src: T,
			          rootElement: String,
			          reverse: Boolean = true,
			          noinline expected: XmlExplorerController.(rootAttrs: Map<String, String>) -> R): R {
		return assertXml(src, reverse) {
			exploreDocument(rootElement, expected)
		}
	}
	
	fun assertAttrs(actual: Map<String, String>, vararg expected: Pair<String, String>) {
		assertEquals(mapOf(*expected), actual)
	}
	
	fun assertNoAttrs(actual: Map<String, String>) {
		assertEquals(emptyMap<String, String>(), actual)
	}
	
	@Test
	fun testEmpty() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable
		assertXml(Mock(), "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
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
	fun textUnknownElemsType() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Elements("s")
			var s = mutableListOf<Any>(System.getSecurityManager())
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
			assertNoAttrs(rootAttrs)
			forEachElement { tag, attrs ->
				assertNoAttrs(attrs)
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
			assertNoAttrs(rootAttrs)
			assertEquals("123", text())
		}
		@RootElement("mock")
		class Mock3 : XmlAutoSerializable {
			@TextBody
			var value: String? = null
		}
		assertXml(Mock3(), "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
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
			assertNoAttrs(rootAttrs)
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
			assertNoAttrs(rootAttrs)
			val things = collectElements { tag, attrs ->
				when (tag) {
					"thing" -> {
						assertNoAttrs(attrs)
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
						assertNoAttrs(attrs)
						tag to text()
					}
			)
		}
	}
	
	@Test
	fun testWrapped() {
		class Inner(@TextBody var content: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Outer(vararg init: String) : XmlAutoSerializable {
			@Elements("e", true, "inners")
			val things: MutableList<Inner> = init.map { Inner(it) }.toMutableList()
		}
		assertXml(Outer("a", "b"), "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			collectOneElement { tag, attrs ->
				when (tag) {
					"inners" -> {
						assertNoAttrs(attrs)
						assertEquals(listOf("a", "b"),
						             collectElements { tag2, attrs2 ->
							             assertEquals("e", tag2)
							             assertNoAttrs(attrs2)
							             text()
						             })
					}
					else -> error("Unexpected $tag")
				}
			}
		}
	}
	
	enum class Foo {
		VALUE1, BAR, BAZ
	}
	
	@Test
	fun testEnums() {
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			@Attribute
			var foo: Foo = Foo.VALUE1
		}
		assertXml(Mock(), "mock") { rootAttrs ->
			assertAttrs(rootAttrs, "foo" to "VALUE1")
			assertNoElements()
		}
	}
	
	@Test
	fun testPolymorphicElements() {
		class Gun(@Attribute var name: String = "") : XmlAutoSerializable
		class Sword(@TextBody var name: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Mock(vararg init: XmlAutoSerializable) : XmlAutoSerializable {
			@PolymorphicElements(polymorphisms = [Polymorphism("gun", Gun::class), Polymorphism("sword", Sword::class)])
			val things = mutableListOf(*init)
		}
		assertXml(Mock(Sword("Excalibur"), Gun("Railgun"), Gun("Trigun")), "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			assertEquals(
					listOf("sword Excalibur", "gun Railgun", "gun Trigun"),
					collectElements { tag, attrs ->
						when (tag) {
							"gun" -> {
								assertEquals(setOf("name"), attrs.keys)
								assertNoElements()
								"gun ${attrs["name"]!!}"
							}
							"sword" -> {
								assertNoAttrs(attrs)
								"sword ${text()}"
							}
							else -> error("unexpected $tag")
						}
					}
			)
		}
	}
	
	@Test
	fun testPolymorphicElementsWrapped() {
		class Gun(@Attribute var name: String = "") : XmlAutoSerializable
		class Sword(@TextBody var name: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Mock(vararg init: XmlAutoSerializable) : XmlAutoSerializable {
			@PolymorphicElements(true, "",
			                     Polymorphism("gun", Gun::class), Polymorphism("sword", Sword::class))
			val things = mutableListOf(*init)
		}
		assertXml(Mock(Sword("Excalibur"), Gun("Railgun"), Gun("Trigun")), "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			collectOneElement { wtag, wattrs ->
				assertEquals("things", wtag)
				assertNoAttrs(wattrs)
				assertEquals(
						listOf("sword Excalibur", "gun Railgun", "gun Trigun"),
						collectElements { tag, attrs ->
							when (tag) {
								"gun" -> {
									assertEquals(setOf("name"), attrs.keys)
									assertNoElements()
									"gun ${attrs["name"]!!}"
								}
								"sword" -> {
									assertNoAttrs(attrs)
									"sword ${text()}"
								}
								else -> error("unexpected $tag")
							}
						}
				)
			}
		}
	}
	
	@Test
	fun testMixedBody() {
		class Gun(@Attribute var name: String = "") : XmlAutoSerializable
		class Sword(@TextBody var name: String = "") : XmlAutoSerializable
		@RootElement("mock")
		class Mock(vararg init: Any) : XmlAutoSerializable {
			@MixedBody(polymorphisms = [Polymorphism("gun", Gun::class), Polymorphism("sword", Sword::class)])
			val things = mutableListOf(*init)
		}
		assertXml(Mock(
				Sword("Excalibur"),
				"vs",
				Gun("Railgun"),
				"and",
				Gun("Trigun")),
		          "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			assertEquals(
					listOf("sword Excalibur", "vs", "gun Railgun", "and", "gun Trigun"),
					collectNodes { node ->
						node.fold(
								{ it },
								{ (tag, attrs) ->
									
									when (tag) {
										"gun" -> {
											assertEquals(setOf("name"), attrs.keys)
											assertNoElements()
											"gun ${attrs["name"]!!}"
										}
										"sword" -> {
											assertNoAttrs(attrs)
											"sword ${text()}"
										}
										else -> error("unexpected $tag")
									}
								}
						)
					}
			)
		}
	}
	interface Thing : XmlAutoSerializable
	@Test
	fun testMixedBodyWithTextConverter() {
		class Gun(@Attribute var name: String = "") : Thing
		class Sword(@TextBody var name: String = "") : Thing
		class Word(var data: String = "") : Thing
		class WordConverter:TextConverter<Word> {
			override fun convert(s: String): Word = Word(s)
			
			override fun toString(a: Word?): String? = a?.data
			
		}
		@RootElement("mock")
		class Mock(vararg init: Thing) : XmlAutoSerializable {
			@MixedBody(WordConverter::class,
					Polymorphism("gun", Gun::class), Polymorphism("sword", Sword::class))
			val things = mutableListOf(*init)
		}
		assertXml(Mock(
				Sword("Excalibur"),
				Word("vs"),
				Gun("Railgun"),
				Word("and"),
				Gun("Trigun")),
		          "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			assertEquals(
					listOf("sword Excalibur", "vs", "gun Railgun", "and", "gun Trigun"),
					collectNodes { node ->
						node.fold(
								{ it },
								{ (tag, attrs) ->
									
									when (tag) {
										"gun" -> {
											assertEquals(setOf("name"), attrs.keys)
											assertNoElements()
											"gun ${attrs["name"]!!}"
										}
										"sword" -> {
											assertNoAttrs(attrs)
											"sword ${text()}"
										}
										else -> error("unexpected $tag")
									}
								}
						)
					}
			)
		}
	}
	
	@Test
	fun testTextBodyWhitespace() {
		val src =
				"<?xml version='1.0' encoding='utf-8' ?>" +
						"<mock>\n" +
						"    skidaddle\n" +
						"    skidoodle\n" +
						"</mock>"
		
		@RootElement("mock")
		data class MockKeep(
				@TextBody
				@TextBodyWhitespacePolicy(WhitespacePolicy.KEEP)
				var things: String = ""
		) : XmlAutoSerializable
		assertDeserialization(MockKeep("\n    skidaddle\n    skidoodle\n"), src)
		
		@RootElement("mock")
		data class MockCompact(
				@TextBody
				@TextBodyWhitespacePolicy(WhitespacePolicy.COMPACT)
				var things: String = ""
		) : XmlAutoSerializable
		assertDeserialization(MockCompact(" skidaddle skidoodle "), src)
		
		@RootElement("mock")
		data class MockTrim(
				@TextBody
				@TextBodyWhitespacePolicy(WhitespacePolicy.TRIM)
				var things: String = ""
		) : XmlAutoSerializable
		assertDeserialization(MockTrim("skidaddle\n    skidoodle"), src)
	}
	
	@Test
	fun testMixedBodyWhitespace() {
		val src =
				"<?xml version='1.0' encoding='utf-8' ?>" +
						"<mock>\n" +
						"    <thing name='a'/>\n" +
						"    then\n" +
						"    <thing name='b'/>\n" +
						"    <thing name='c'/>\n" +
						"    .\n" +
						"    \n" +
						"    <thing name='d'/>\n" +
						"</mock>"
		
		data class Thing(@Attribute var name: String = "") : XmlAutoSerializable
		@RootElement("mock")
		data class MockKeep(
				@MixedToEitherBody(Polymorphism("thing", Thing::class))
				@MixedBodyWhitespacePolicy(WhitespacePolicy.KEEP)
				val things: MutableList<Either<String, Thing>>
		) : XmlAutoSerializable {
			constructor(vararg things: Either<String, Thing>) : this(mutableListOf(*things))
		}
		assertDeserialization(
				MockKeep(
						"\n    ".iAmEitherLeft(),
						Thing("a").iAmEitherRight(),
						"\n    then\n    ".iAmEitherLeft(),
						Thing("b").iAmEitherRight(),
						"\n    ".iAmEitherLeft(),
						Thing("c").iAmEitherRight(),
						"\n    .\n    \n    ".iAmEitherLeft(),
						Thing("d").iAmEitherRight(),
						"\n".iAmEitherLeft()
				),
				src
		)
		@RootElement("mock")
		data class MockCompact(
				@MixedToEitherBody(Polymorphism("thing", Thing::class))
				@MixedBodyWhitespacePolicy(WhitespacePolicy.COMPACT)
				val things: MutableList<Either<String, Thing>>
		) : XmlAutoSerializable {
			constructor(vararg things: Either<String, Thing>) : this(mutableListOf(*things))
		}
		assertDeserialization(
				MockCompact(
						" ".iAmEitherLeft(),
						Thing("a").iAmEitherRight(),
						" then ".iAmEitherLeft(),
						Thing("b").iAmEitherRight(),
						" ".iAmEitherLeft(),
						Thing("c").iAmEitherRight(),
						" . ".iAmEitherLeft(),
						Thing("d").iAmEitherRight(),
						" ".iAmEitherLeft()
				),
				src
		)
		@RootElement("mock")
		data class MockTrim(
				@MixedToEitherBody(Polymorphism("thing", Thing::class))
				@MixedBodyWhitespacePolicy(WhitespacePolicy.TRIM)
				val things: MutableList<Either<String, Thing>>
		) : XmlAutoSerializable {
			constructor(vararg things: Either<String, Thing>) : this(mutableListOf(*things))
		}
		assertDeserialization(
				MockTrim(
						Thing("a").iAmEitherRight(),
						"then".iAmEitherLeft(),
						Thing("b").iAmEitherRight(),
						Thing("c").iAmEitherRight(),
						".".iAmEitherLeft(),
						Thing("d").iAmEitherRight()
				),
				src
		)
		
	}
	@Test
	fun testMixedBodyEither() {
		abstract class Thing
		class Gun(@Attribute var name: String = "") : Thing(),XmlAutoSerializable
		class Sword(@TextBody var name: String = "") : Thing(),XmlAutoSerializable
		@RootElement("mock")
		class Mock(vararg init: Either<String, Thing>) : XmlAutoSerializable {
			@MixedToEitherBody(Polymorphism("gun", Gun::class),
			                   Polymorphism("sword", Sword::class))
			val things = mutableListOf(*init)
		}
		assertXml(Mock(
				Sword("Excalibur").iAmEitherRight(),
				"vs".iAmEitherLeft(),
				Gun("Railgun").iAmEitherRight(),
				"and".iAmEitherLeft(),
				Gun("Trigun").iAmEitherRight()),
		          "mock") { rootAttrs ->
			assertNoAttrs(rootAttrs)
			assertEquals(
					listOf("sword Excalibur", "vs", "gun Railgun", "and", "gun Trigun"),
					collectNodes { node ->
						node.fold(
								{ it },
								{ (tag, attrs) ->
									
									when (tag) {
										"gun" -> {
											assertEquals(setOf("name"), attrs.keys)
											assertNoElements()
											"gun ${attrs["name"]!!}"
										}
										"sword" -> {
											assertNoAttrs(attrs)
											"sword ${text()}"
										}
										else -> error("unexpected $tag")
									}
								}
						)
					}
			)
		}
	}
	@Test
	fun testBeforeAfter() {
		class Inner : XmlAutoSerializable {
			@BeforeSave
			private fun f1() {
				log += "bsi"
				c = "($c)"
			}
			@Attribute("c")
			var c = "d"
			@AfterSave
			private fun f2() {
				c = c.removeSurrounding("(",")")
				log += "asi"
			}
			private var tmpParent:Any? = null
			@BeforeLoad
			private fun f3(parent:Any?) {
				assertNotNull(parent)
				assertEquals("mock", parent!!.toString())
				tmpParent = parent
				log += "bli"
			}
			@AfterLoad
			private fun f4(parent:Any?) {
				c = c.removeSurrounding("(",")")
				assertEquals(tmpParent,parent)
				log += "ali"
			}
		}
		@RootElement("mock")
		class Mock : XmlAutoSerializable {
			override fun toString() = "mock"
			@Attribute("a")
			var a = "b"
			@BeforeSave
			private fun bso() {
				log += "bso"
			}
			@AfterSave
			private  fun aso() {
				log += "aso"
			}
			@Element("inner")
			var inner = Inner()
			@BeforeLoad
			private fun blo() {
				log += "blo"
			}
			@AfterLoad
			private fun alo() {
				log += "alo"
			}
		}
		assertXml(Mock(),"mock") { rootAttrs ->
			assertAttrs(rootAttrs,"a" to "b")
			collectOneElement { tag, attrs ->
				assertAttrs(attrs,"c" to "(d)")
				assertEquals("inner",tag)
				assertNoElements()
			}
		}
		assertEquals("bso" + "bsi" + "asi" + "aso" +
				             "blo" + "bli" + "ali" + "alo" +
				             // 2nd round of saving performed by assertXML to check serialized forms
				             "bso" + "bsi" + "asi" + "aso",
		             log)
	}
	@Before
	fun clearLog() {
		log = ""
	}
}
var log = ""