package ej.utils

/*
 * Created by aimozg on 31.07.2018.
 * Confidential until published on GitHub
 */
interface Validable {
	fun validate():ValidationEntry
}
sealed class ValidationEntry(val isValid:Boolean) {
	fun validationErrors():List<String> =
			if (isValid) emptyList()
			else when(this) {
				is SimpleValidationEntry -> listOf(reason)
				is ValidationReport -> entries.flatMap { it.validationErrors() }
				is WrappedValidationEntry -> wrapped.validationErrors().map {
					"$location $it"
				}
			}
}
class SimpleValidationEntry(val reason:String,isValid:Boolean):ValidationEntry(isValid)
class WrappedValidationEntry(val location:String,val wrapped:ValidationEntry):ValidationEntry(wrapped.isValid)
class ValidationReport(
		isValid:Boolean,
		val entries:List<ValidationEntry>
): ValidationEntry(isValid) {
	constructor(entries:List<ValidationEntry>):this(
			entries.all { it.isValid },
			entries
	)
	companion object {
		inline fun build(full:Boolean=false,
		                 init:ValidationReportBuilder.()->Unit):ValidationReport =
				ValidationReportBuilder(full).apply(init).build()
	}
}
class ValidationReportBuilder(val full:Boolean) {
	private var isValid = true
	private val entries = ArrayList<ValidationEntry>()
	fun build() = ValidationReport(isValid,entries)
	
	fun<T:Validable> validateAll(items:Iterable<T>,collection:String,itemNamer:(T)->String) {
		if (!full && !isValid) return
		for (item in items) {
			val e = item.validate()
			if (!e.isValid) {
				entries += WrappedValidationEntry(collection+" "+itemNamer(item),e)
				isValid = false
				if (!full) break
			}
		}
	}
	fun validateAll(items:Iterable<Validable>, collection:String="items") {
		if (!full && !isValid) return
		for ((i,item) in items.withIndex()) {
			val e = item.validate()
			if (!e.isValid) {
				entries += WrappedValidationEntry("$collection[$i]", e)
				isValid = false
				if (!full) break
			}
		}
	}
	fun validateAll(items:Sequence<Validable>, collection:String="items") {
		validateAll(items.asIterable(), collection)
	}
	fun validate(item:Validable,name:String=item.toString()) {
		if (!full && !isValid) return
		val e = item.validate()
		if (!e.isValid) {
			entries += WrappedValidationEntry(name,e)
			isValid = false
		}
	}
	fun markInvalid(reason:String) {
		entries += SimpleValidationEntry(reason,false)
		isValid = false
	}
	fun assertEquals(expected:Any?,actual:Any?) =
			assertEquals("",expected,actual)
	fun assertEquals(message:String, expected:Any?,actual:Any?) {
		if (expected != actual) markInvalid("$message expected: $expected, but was: $actual")
	}
	fun assertNotNull(actual:Any?) =
			assertNotNull("", actual)
	fun assertNotNull(message: String, actual:Any?) {
		if (actual == null) markInvalid("$message is null")
	}
	
}
