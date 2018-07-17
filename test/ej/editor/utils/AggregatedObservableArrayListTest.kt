package ej.editor.utils

import javafx.beans.property.SimpleObjectProperty
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import tornadofx.*

/*
 * Created by aimozg on 17.07.2018.
 * Confidential until published on GitHub
 */
class AggregatedObservableArrayListTest {
	@Test
	fun testSurvivesGc() {
		val a = listOf("foo").observable()
		val b = arrayListOf("alice","bob").observable()
		val cp = SimpleObjectProperty<String?>("last")
		val c = ObservableSingletonList(cp)
		val rememberMe = observableConcatenation(a, b, c)
		val total = ArrayList<String>().apply {
			bind(rememberMe){it}
		}
		assertEquals(a+b+c,total)
		b.add("charlie")
		System.gc()
		assertEquals(a+b+c,total)
		b.add("david")
		System.gc()
		assertEquals(a+b+c,total)
		b.remove("bob")
		System.gc()
		assertEquals(a+b+c,total)
		cp.set(null)
		assertTrue(c.isEmpty())
		System.gc()
		assertEquals(a+b+c,total)
	}
}